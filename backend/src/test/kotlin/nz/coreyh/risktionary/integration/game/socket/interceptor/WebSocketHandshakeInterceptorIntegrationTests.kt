package nz.coreyh.risktionary.integration.game.socket.interceptor

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.service.session.GameSessionService
import nz.coreyh.risktionary.game.application.session.GameJoinCredentials
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.shared.web.dto.ApiErrorResponse
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import nz.coreyh.risktionary.support.extensions.andReturn
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

@IntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketHandshakeInterceptorIntegrationTests(
    @LocalServerPort private val port: Int,
    private val mockMvc: MockMvc,
    private val gameSessionService: GameSessionService,
    private val gameTicketService: GameTicketService,
    private val testGameSessionCreator: TestGameSessionCreator,
    private val objectMapper: ObjectMapper,
) {
    @Test
    fun `handshake fails when ticket is not provided`() {
        val response =
            mockMvc
                .get(Routes.V1.Game.SOCKET) {
                    websocketUpgradeHeaders()
                }.andExpect {
                    status { isBadRequest() }
                }.andReturn<ApiErrorResponse>(objectMapper)

        response.errorCode shouldBe ErrorCode.GAME_TICKET_INVALID.code
    }

    @Test
    fun `handshake fails when ticket is invalid`() {
        val response =
            mockMvc
                .get(Routes.V1.Game.SOCKET) {
                    websocketUpgradeHeaders()
                    param("ticket", "invalid")
                }.andExpect {
                    status { isBadRequest() }
                }.andReturn<ApiErrorResponse>(objectMapper)

        response.errorCode shouldBe ErrorCode.GAME_TICKET_INVALID.code
    }

    @Test
    fun `handshake fails when ticket references a non-existent game`() {
        val ticket = gameTicketService.generateTicket(createTestGameId(), createTestGamePlayerId())

        val response =
            mockMvc
                .get(Routes.V1.Game.SOCKET) {
                    websocketUpgradeHeaders()
                    param("ticket", ticket.value)
                }.andExpect {
                    status { isBadRequest() }
                }.andReturn<ApiErrorResponse>(objectMapper)

        response.errorCode shouldBe ErrorCode.GAME_TICKET_INVALID.code
    }

    @Test
    fun `handshake fails when ticket references a player that has not joined the game`() {
        val session = testGameSessionCreator.createTestGameSession()
        val ticket = gameTicketService.generateTicket(session.gameId, createTestGamePlayerId())

        val response =
            mockMvc
                .get(Routes.V1.Game.SOCKET) {
                    websocketUpgradeHeaders()
                    param("ticket", ticket.value)
                }.andExpect {
                    status { isBadRequest() }
                }.andReturn<ApiErrorResponse>(objectMapper)

        response.errorCode shouldBe ErrorCode.GAME_TICKET_INVALID.code
    }

    @Test
    fun `handshake succeeds with valid ticket`() {
        val session = testGameSessionCreator.createTestGameSession()
        val player = gameSessionService.join(session.code, GameJoinCredentials.Guest("Guest"))
        val ticket = gameTicketService.generateTicket(session.gameId, player.playerId)
        val client = WebSocketStompClient(StandardWebSocketClient())

        val result =
            client.connectAsync(
                "ws://localhost:$port${Routes.V1.Game.SOCKET}?ticket=${ticket.value}",
                object : StompSessionHandlerAdapter() {},
            )

        assertDoesNotThrow {
            result.get(2, TimeUnit.SECONDS)
        }
    }

    private fun MockHttpServletRequestDsl.websocketUpgradeHeaders() {
        header("Upgrade", "websocket")
        header("Connection", "Upgrade")
        header("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==")
        header("Sec-WebSocket-Version", "13")
    }
}
