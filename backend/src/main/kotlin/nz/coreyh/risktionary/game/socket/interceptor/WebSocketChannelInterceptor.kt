package nz.coreyh.risktionary.game.socket.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

private val kLogger = KotlinLogging.logger {}

/**
 * This interceptor prevents clients from subscribing to topics belonging to other games, ensuring
 * strict isolation between game sessions and protecting in‑game events from unauthorized listeners.
 */
@Component
class WebSocketChannelInterceptor : ChannelInterceptor {
    /**
     * Validates STOMP SUBSCRIBE messages sent through the WebSocket inbound channel.
     *
     * This interceptor ensures that a client may only subscribe to destinations belonging to the game
     * they authenticated into during the WebSocket handshake.
     *
     * Rejects the subscription if:
     * - `gameId` is missing from the session (unauthenticated session)
     * - The destination header is missing
     * - The destination does not begin with the expected game‑scoped prefix
     *
     * On rejection a [MessageDeliveryException] is thrown, preventing the subscription from being registered.
     */
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel,
    ): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        when (accessor.command) {
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
