package nz.coreyh.risktionary.game.socket.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketConnectRejectedException
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import nz.coreyh.risktionary.game.socket.support.WebSocketSessionAttributes
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

private val kLogger = KotlinLogging.logger {}

/**
 * Channel interceptor that validates STOMP CONNECT and SUBSCRIBE messages on the WebSocket inbound channel.
 *
 * CONNECT is refused (surfacing to the client as a STOMP ERROR frame carrying an error code) when
 * [WebSocketHandshakeInterceptor] recorded a failure or no [GameSocketPrincipal] was established.
 *
 * This interceptor enforces strict isolation between game sessions by ensuring clients can only
 * subscribe to destinations belonging to their authenticated game or user-specific queues.
 * It acts as a security boundary preventing cross-game eavesdropping and unauthorized subscription
 * attempts.
 */
@Component
class WebSocketChannelInterceptor : ChannelInterceptor {
    /**
     * Validates a STOMP SUBSCRIBE message before it is processed by the message broker.
     *
     * The validation ensures that a client's subscription destination is authorized based on
     * their authenticated [GameSocketPrincipal]:
     * - Player principals can subscribe to their game-specific topics and user-specific queues
     * - Host principals can subscribe to their game-specific topics and user-specific queues
     *
     * A subscription is rejected with a [MessageDeliveryException] if:
     * - The WebSocket session lacks a [GameSocketPrincipal] (unauthenticated session)
     * - The STOMP message has no destination header
     * - The destination does not start with either:
     *   - The game-scoped topic prefix for their game (e.g., `/topic/games/{gameId}`)
     *   - The user-scoped queue prefix (e.g., `/user/queue/...`)
     *
     * User queues are always permitted regardless of game ID, as they represent private
     * message channels scoped to the individual client connection.
     *
     * @param message The incoming STOMP message to validate
     * @param channel The message channel through which the message is being sent
     * @return The original message if validation passes, or throws an exception
     * @throws MessageDeliveryException When subscription is unauthorized (missing principal,
     *                                  missing destination, or invalid destination prefix)
     */
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel,
    ): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        when (accessor.command) {
            StompCommand.CONNECT -> {
                val refusal = accessor.sessionAttributes?.get(WebSocketSessionAttributes.CONNECT_ERROR) as? ErrorCode
                if (refusal != null) {
                    kLogger.debug { "Rejecting CONNECT: $refusal (sessionId=${accessor.sessionId})" }
                    throw WebSocketConnectRejectedException(refusal)
                }
                if (accessor.user !is GameSocketPrincipal) {
                    kLogger.debug { "Rejecting CONNECT: missing principal (sessionId=${accessor.sessionId})" }
                    throw WebSocketConnectRejectedException(ErrorCode.AUTH_UNAUTHENTICATED)
                }
            }

            StompCommand.SUBSCRIBE -> {
                val principal =
                    accessor.user as? GameSocketPrincipal
                        ?: throw MessageDeliveryException("Missing principal")
                val destination =
                    accessor.destination
                        ?: run {
                            kLogger.debug {
                                "Rejecting subscription: missing destination (sessionId=${accessor.sessionId})"
                            }
                            throw MessageDeliveryException("Unauthorized subscription destination")
                        }

                val expectedGamePrefix = WebSocketDestinations.Topic.base(principal.gameId)
                val expectedQueuePrefix = "${WebSocketDestinations.USER_PREFIX}${WebSocketDestinations.Queue.PREFIX}"
                if (
                    !destination.startsWith(expectedGamePrefix) &&
                    !destination.startsWith(expectedQueuePrefix)
                ) {
                    kLogger.debug {
                        "Rejecting subscription: destination=$destination " +
                            "(gameId=${principal.gameId}, sessionId=${accessor.sessionId})"
                    }
                    throw MessageDeliveryException("Unauthorized subscription destination")
                }
            }

            else -> {}
        }
        return message
    }
}
