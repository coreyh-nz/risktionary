package nz.coreyh.risktionary.integration.game.socket.interceptor

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.auth.application.service.AuthTokenService
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.shared.web.dto.ApiErrorResponse
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import nz.coreyh.risktionary.support.creator.TestUserCreator
import nz.coreyh.risktionary.support.extensions.andReturn
import nz.coreyh.risktionary.support.extensions.auth
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.factory.user.createTestUser
import nz.coreyh.risktionary.support.socket.WebSocketTestSupport.connect
import nz.coreyh.risktionary.user.domain.model.User
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.socket.WebSocketHttpHeaders
import tools.jackson.databind.ObjectMapper

@IntegrationTest
@Transactional
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["logging.level.nz.coreyh.risktionary=debug"],
)
class WebSocketHandshakeInterceptorIntegrationTests(
    @LocalServerPort private val port: Int,
    private val mockMvc: MockMvc,
    private val gameSessionService: GameSessionService,
    private val gameTicketService: GameTicketService,
    private val authTokenService: AuthTokenService,
    private val testGameSessionCreator: TestGameSessionCreator,
    private val testUserCreator: TestUserCreator,
    private val authConfiguration: AuthConfiguration,
    private val objectMapper: ObjectMapper,
) {
    @Nested
    inner class Player {
        @Test
        fun `handshake succeeds with valid ticket`() {
            val session = testGameSessionCreator.createTestGameSession()
            val player = gameSessionService.handleJoinRequest(session.code, createTestGamePlayerIdentityGuest())
            val ticket = gameTicketService.generateTicket(session.id, player.playerId)

            assertDoesNotThrow {
                connect(port, ticket.value)
            }
        }

        @Test
        fun `handshake fails when ticket is not provided and user is unauthenticated`() {
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
        fun `handshake fails when ticket is an empty string`() {
            val response =
                mockMvc
                    .get(Routes.V1.Game.SOCKET) {
                        websocketUpgradeHeaders()
                        param("ticket", "")
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
                        param("ticket", "not-a-valid-ticket")
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
            val ticket = gameTicketService.generateTicket(session.id, createTestGamePlayerId())

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
    }

    @Nested
    inner class Host {
        @Test
        fun `host handshake succeeds when authenticated host has an active game`() {
            val user = testUserCreator.createTestUser()
            testGameSessionCreator.createTestGameSession(host = user)

            assertDoesNotThrow {
                connect(port, ticket = null, handshakeHeaders = buildHostHandshakeHeaders(user))
            }
        }

        @Test
        fun `host handshake fails when user is not authenticated`() {
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
        fun `host handshake fails when authenticated user has no active game`() {
            val user = createTestUser()

            val response =
                mockMvc
                    .get(Routes.V1.Game.SOCKET) {
                        websocketUpgradeHeaders()
                        auth(user)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andReturn<ApiErrorResponse>(objectMapper)

            response.errorCode shouldBe ErrorCode.GAME_TICKET_INVALID.code
        }
    }

    private fun MockHttpServletRequestDsl.websocketUpgradeHeaders() {
        header("Upgrade", "websocket")
        header("Connection", "Upgrade")
        header("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==")
        header("Sec-WebSocket-Version", "13")
    }

    private fun buildHostHandshakeHeaders(user: User): WebSocketHttpHeaders =
        WebSocketHttpHeaders().apply {
            val token = authTokenService.generateAccessToken(user)
            add(HttpHeaders.COOKIE, "${authConfiguration.cookie.accessTokenName}=${token.value}")
        }
}
