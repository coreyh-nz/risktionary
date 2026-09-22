package nz.coreyh.risktionary.game.socket.interceptor

import nz.coreyh.risktionary.game.socket.support.WebSocketConnectRejectedException
import nz.coreyh.risktionary.game.socket.support.WebSocketSessionAttributes
import org.springframework.messaging.Message
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler

/**
 * Builds the STOMP ERROR frame sent to a client whose CONNECT was refused with a [WebSocketConnectRejectedException].
 *
 * The frame carries a human-readable `message` header and a machine-readable `code` header
 * (see [nz.coreyh.risktionary.shared.exception.code.ErrorCode.code]). Any other failure falls back to Spring's default.
 */
@Component
class WebSocketStompErrorHandler : StompSubProtocolErrorHandler() {
    override fun handleClientMessageProcessingError(
        clientMessage: Message<ByteArray>?,
        ex: Throwable,
    ): Message<ByteArray>? {
        val rejection =
            generateSequence(ex) { it.cause }
                .filterIsInstance<WebSocketConnectRejectedException>()
                .firstOrNull()
                ?: return super.handleClientMessageProcessingError(clientMessage, ex)

        val accessor = StompHeaderAccessor.create(StompCommand.ERROR)
        accessor.message = rejection.errorCode.defaultMessage
        accessor.setNativeHeader(WebSocketSessionAttributes.ERROR_CODE_HEADER, rejection.errorCode.code)
        accessor.setImmutable()
        return MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)
    }
}
