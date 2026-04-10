package nz.coreyh.risktionary.integration.game.socket.interceptor

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.service.session.GameSessionService
import nz.coreyh.risktionary.game.application.session.GameJoinCredentials
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@IntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketChannelInterceptorIntegrationTests(
    @LocalServerPort private val port: Int,
    private val gameSessionService: GameSessionService,
    private val gameTicketService: GameTicketService,
    private val testGameSessionCreator: TestGameSessionCreator,
) {
    @Test
    fun `subscription succeeds when subscribing to correct destination`() {
        val session = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.join(session.code, GameJoinCredentials.Guest("Guest"))
        val ticket = gameTicketService.generateTicket(session.gameId, player.playerId)

        val client = connect(ticket.value)
        val expectedDestination = WebSocketDestinations.Topic.lobby(session.gameId)
        client.subscribe(expectedDestination, stompFrameHandler)

        // give it a moment to ensure no error frame arrives
        Thread.sleep(500)
        client.isConnected.shouldBeTrue()
    }

    @Test
    fun `subscription fails when subscribing to a wrong destination`() {
        val session = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.join(session.code, GameJoinCredentials.Guest("Guest"))
        val ticket = gameTicketService.generateTicket(session.gameId, player.playerId)

        val disconnected = CompletableFuture<Throwable>()
        val client = connect(ticket.value, onError = { disconnected.complete(it) })
        client.subscribe("/topic/invalid", stompFrameHandler)

        val error = disconnected.get(2, TimeUnit.SECONDS)
        error.shouldNotBeNull()
    }

    @Test
    fun `subscription to another game's topic is rejected`() {
        val session1 = testGameSessionCreator.createTestGameSession()
        val session2 = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.join(session1.code, GameJoinCredentials.Guest("Guest"))
        val ticket = gameTicketService.generateTicket(session1.gameId, player.playerId)

        val disconnected = CompletableFuture<Throwable>()
        val client = connect(ticket.value, onError = { disconnected.complete(it) })
        client.subscribe(WebSocketDestinations.Topic.lobby(session2.gameId), stompFrameHandler)

        val error = disconnected.get(2, TimeUnit.SECONDS)
        error.shouldNotBeNull()
    }

    private fun connect(
        ticket: String,
        onError: (Throwable) -> Unit = {},
    ): StompSession {
        val client = WebSocketStompClient(StandardWebSocketClient())
        val future = CompletableFuture<StompSession>()

        client.connectAsync(
            "ws://localhost:$port${Routes.V1.Game.SOCKET}?ticket=$ticket",
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

        return future.get(2, TimeUnit.SECONDS)
    }

    private val stompFrameHandler: StompFrameHandler =
        object : StompFrameHandler {
            override fun getPayloadType(headers: StompHeaders) = String::class.java

            override fun handleFrame(
                headers: StompHeaders,
                payload: Any?,
            ) {
            }
        }
}
