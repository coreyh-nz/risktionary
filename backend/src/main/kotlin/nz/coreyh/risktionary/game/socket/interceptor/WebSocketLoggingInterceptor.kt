package nz.coreyh.risktionary.game.socket.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor

private val kLogger = KotlinLogging.logger {}

enum class WebSocketLoggingDirection {
    INBOUND,
    OUTBOUND,
}

class WebSocketLoggingInterceptor(
    private val direction: WebSocketLoggingDirection,
) : ChannelInterceptor {
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel,
    ): Message<*> {
        val accessor = StompHeaderAccessor.wrap(message)

        kLogger.trace {
            val payload =
                when (val p = message.payload) {
                    is ByteArray -> String(p)
                    else -> p.toString()
                }
            when (direction) {
                WebSocketLoggingDirection.INBOUND -> {
                    "${"[$direction]".padEnd(10)} " +
                        "${accessor.command?.name?.padEnd(16)} " +
                        "user=${accessor.user?.name ?: "anonymous"} " +
                        "destination=${accessor.destination} " +
                        "payload=$payload"
                }

                WebSocketLoggingDirection.OUTBOUND -> {
                    "${"[$direction]".padEnd(10)} " +
                        "destination=${accessor.destination} " +
                        "payload=$payload"
                }
            }
        }

        return message
    }
}
