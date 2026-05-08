package nz.coreyh.risktionary.support.socket

import nz.coreyh.risktionary.shared.web.support.Routes
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object WebSocketTestSupport {
    const val CONNECT_TIMEOUT_SECONDS = 2L
    const val FRAME_SETTLE_MS = 250L

    fun connect(
        port: Int,
        ticket: String? = null,
        gameId: String? = null,
        handshakeHeaders: WebSocketHttpHeaders = WebSocketHttpHeaders(),
        onError: (Throwable) -> Unit = {},
    ): StompSession {
        val client = WebSocketStompClient(StandardWebSocketClient())
        val future = CompletableFuture<StompSession>()
        val url = buildUrl(port, ticket, gameId)

        client.connectAsync(
            url,
            handshakeHeaders,
            object : StompSessionHandlerAdapter() {
                override fun afterConnected(
                    session: StompSession,
                    connectedHeaders: StompHeaders,
                ) {
                    future.complete(session)
                }

                override fun handleTransportError(
                    session: StompSession,
                    exception: Throwable,
                ) {
                    onError(exception)
                    future.completeExceptionally(exception)
                }
            },
        )

        return future.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }

    val noopFrameHandler: StompFrameHandler =
        object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders) = String::class.java

            override fun handleFrame(
                headers: StompHeaders,
                payload: Any?,
            ) {
            }
        }

    private fun buildUrl(
        port: Int,
        ticket: String?,
        gameId: String?,
    ): String {
        val base = "ws://localhost:$port${Routes.V1.Game.SOCKET}"
        val params = listOfNotNull(ticket?.let { "ticket=$it" }, gameId?.let { "gameId=$it" })
        return params
            .takeIf { it.isNotEmpty() }
            ?.let { "$base?${it.joinToString("&")}" } ?: base
    }
}
