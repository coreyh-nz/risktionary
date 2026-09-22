package nz.coreyh.risktionary.integration.game.socket

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.config.HEARTBEAT_INTERVAL_MS
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.socket.WebSocketTestSupport.CONNECT_TIMEOUT_SECONDS
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@IntegrationTest
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketHeartbeatIntegrationTests(
    @LocalServerPort private val port: Int,
    private val gameSessionService: GameSessionService,
    private val gameTicketService: GameTicketService,
    private val testGameSessionCreator: TestGameSessionCreator,
) {
    @Test
    fun `server negotiates heartbeats when the client asks for them`() {
        val session = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.handleJoinRequest(session.code, createTestGamePlayerIdentityGuest())
        val ticket = gameTicketService.generateTicket(session.id, player.playerId)

        val scheduler = ThreadPoolTaskScheduler().apply { initialize() }
        val client =
            WebSocketStompClient(StandardWebSocketClient()).apply {
                taskScheduler = scheduler
                defaultHeartbeat = longArrayOf(HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS)
            }
        val negotiated = CompletableFuture<StompHeaders>()

        client.connectAsync(
            "ws://localhost:$port${Routes.V1.Game.SOCKET}?ticket=${ticket.value}",
            WebSocketHttpHeaders(),
            object : StompSessionHandlerAdapter() {
                override fun afterConnected(
                    session: StompSession,
                    connectedHeaders: StompHeaders,
                ) {
                    negotiated.complete(connectedHeaders)
                }
            },
        )

        try {
            negotiated.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS).heartbeat shouldBe
                longArrayOf(HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS)
        } finally {
            client.stop()
            scheduler.shutdown()
        }
    }
}
