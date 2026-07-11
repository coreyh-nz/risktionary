package nz.coreyh.risktionary.integration.game.socket.interceptor

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.socket.WebSocketTestSupport
import nz.coreyh.risktionary.support.socket.WebSocketTestSupport.CONNECT_TIMEOUT_SECONDS
import nz.coreyh.risktionary.support.socket.WebSocketTestSupport.FRAME_SETTLE_MS
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@IntegrationTest
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketChannelInterceptorIntegrationTests(
    @LocalServerPort private val port: Int,
    private val gameSessionService: GameSessionService,
    private val gameTicketService: GameTicketService,
    private val testGameSessionCreator: TestGameSessionCreator,
) {
    @Test
    fun `subscription succeeds when subscribing to correct topic destination`() {
        val session = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.handleJoinRequest(session.code, createTestGamePlayerIdentityGuest())
        val ticket = gameTicketService.generateTicket(session.id, player.playerId)

        val client = WebSocketTestSupport.connect(port, ticket.value)
        val expectedDestination = WebSocketDestinations.Topic.base(session.id)
        client.subscribe(expectedDestination, WebSocketTestSupport.noopFrameHandler)

        Thread.sleep(FRAME_SETTLE_MS)
        client.isConnected.shouldBeTrue()
    }

    @Test
    fun `subscription succeeds when subscribing to correct user queue destination`() {
        val session = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.handleJoinRequest(session.code, createTestGamePlayerIdentityGuest())
        val ticket = gameTicketService.generateTicket(session.id, player.playerId)

        val client = WebSocketTestSupport.connect(port, ticket.value)
        val expectedDestination = "${WebSocketDestinations.USER_PREFIX}${WebSocketDestinations.Queue.PREFIX}"
        client.subscribe(expectedDestination, WebSocketTestSupport.noopFrameHandler)

        Thread.sleep(FRAME_SETTLE_MS)
        client.isConnected.shouldBeTrue()
    }

    @Test
    fun `subscription fails when subscribing to a wrong destination`() {
        val session = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.handleJoinRequest(session.code, createTestGamePlayerIdentityGuest())
        val ticket = gameTicketService.generateTicket(session.id, player.playerId)

        val disconnected = CompletableFuture<Throwable>()
        val client = WebSocketTestSupport.connect(port, ticket.value, onError = { disconnected.complete(it) })
        client.subscribe("/topic/invalid", WebSocketTestSupport.noopFrameHandler)

        val error = disconnected.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        error.shouldNotBeNull()
    }

    @Test
    fun `subscription to another game's topic is rejected`() {
        val session1 = testGameSessionCreator.createTestGameSession()
        val session2 = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.handleJoinRequest(session1.code, createTestGamePlayerIdentityGuest())
        val ticket = gameTicketService.generateTicket(session1.id, player.playerId)

        val disconnected = CompletableFuture<Throwable>()
        val client = WebSocketTestSupport.connect(port, ticket.value, onError = { disconnected.complete(it) })
        client.subscribe(WebSocketDestinations.Topic.base(session2.id), WebSocketTestSupport.noopFrameHandler)

        val error = disconnected.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        error.shouldNotBeNull()
    }
}
