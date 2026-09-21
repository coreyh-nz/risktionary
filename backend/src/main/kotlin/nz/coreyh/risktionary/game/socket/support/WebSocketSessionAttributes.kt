package nz.coreyh.risktionary.game.socket.support

/**
 * Keys for the attributes stored on a WebSocket session during the HTTP handshake.
 */
object WebSocketSessionAttributes {
    /** The authenticated [nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal], set when validation passes. */
    const val PRINCIPAL = "principal"

    /**
     * The [nz.coreyh.risktionary.shared.exception.code.ErrorCode] describing why the connection was refused, set when
     * validation fails. Browsers cannot read the body of a failed WebSocket upgrade, so the handshake is accepted and
     * the refusal is reported to the client as a STOMP ERROR frame in response to CONNECT.
     */
    const val CONNECT_ERROR = "connectError"

    /** Name of the STOMP ERROR frame header carrying the [nz.coreyh.risktionary.shared.exception.code.ErrorCode.code]. */
    const val ERROR_CODE_HEADER = "code"
}
