package nz.coreyh.risktionary.game.socket.support

import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import org.springframework.messaging.MessagingException

/**
 * Thrown while processing a STOMP CONNECT frame to refuse the connection.
 * Reported to the client as a STOMP ERROR frame by [nz.coreyh.risktionary.game.socket.interceptor.WebSocketStompErrorHandler].
 */
class WebSocketConnectRejectedException(
    val errorCode: ErrorCode,
) : MessagingException(errorCode.defaultMessage)
