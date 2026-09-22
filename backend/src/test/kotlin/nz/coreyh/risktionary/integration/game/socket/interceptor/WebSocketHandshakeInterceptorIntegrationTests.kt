package nz.coreyh.risktionary.integration.game.socket.interceptor

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.auth.application.service.AuthTokenService
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import nz.coreyh.risktionary.support.creator.TestUserCreator
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.factory.user.createTestUser
import nz.coreyh.risktionary.support.socket.WebSocketTestSupport.connect
import nz.coreyh.risktionary.support.socket.WebSocketTestSupport.connectExpectingError
import nz.coreyh.risktionary.user.domain.model.User
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.socket.WebSocketHttpHeaders

@IntegrationTest
@Transactional
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class WebSocketHandshakeInterceptorIntegrationTests(
    @LocalServerPort private val port: Int,
    private val gameSessionService: GameSessionService,
    private val gameTicketService: GameTicketService,
    private val authTokenService: AuthTokenService,
    private val testGameSessionCreator: TestGameSessionCreator,
    private val testUserCreator: TestUserCreator,
    private val authConfiguration: AuthConfiguration,
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
        fun `connect is refused when ticket is not provided and user is unauthenticated`() {
            val headers =
                connectExpectingError(port)

            headers.getFirst("code") shouldBe ErrorCode.INVALID_REQUEST.code
        }

        @Test
        fun `connect is refused when ticket is an empty string`() {
            val headers =
                connectExpectingError(
                    port,
                    ticket = "",
                )

            headers.getFirst("code") shouldBe ErrorCode.GAME_TICKET_INVALID.code
        }

        @Test
        fun `connect is refused when ticket is invalid`() {
            val headers =
                connectExpectingError(
                    port,
                    ticket = "not-a-valid-ticket",
                )

            headers.getFirst("code") shouldBe ErrorCode.GAME_TICKET_INVALID.code
        }

        @Test
        fun `connect is refused when ticket references a non-existent game`() {
            val ticket = gameTicketService.generateTicket(createTestGameId(), createTestGamePlayerId())

            val headers =
                connectExpectingError(
                    port,
                    ticket = ticket.value,
                )

            headers.getFirst("code") shouldBe ErrorCode.GAME_NOT_FOUND.code
        }

        @Test
        fun `connect is refused when ticket references a player that has not joined the game`() {
            val session = testGameSessionCreator.createTestGameSession()
            val ticket = gameTicketService.generateTicket(session.id, createTestGamePlayerId())

            val headers =
                connectExpectingError(
                    port,
                    ticket = ticket.value,
                )

            headers.getFirst("code") shouldBe ErrorCode.GAME_PLAYER_NOT_IN_SESSION.code
        }
    }

    @Nested
    inner class Host {
        @Test
        fun `host handshake succeeds when authenticated host has an active game`() {
            val user = testUserCreator.createTestUser()
            val session = testGameSessionCreator.createTestGameSession(host = user)

            assertDoesNotThrow {
                connect(
                    port,
                    ticket = null,
                    gameId = session.id.value.toString(),
                    handshakeHeaders = buildHostHandshakeHeaders(user),
                )
            }
        }

        @Test
        fun `host connect is refused when user is not authenticated`() {
            val session = testGameSessionCreator.createTestGameSession()

            val headers =
                connectExpectingError(
                    port,
                    gameId = session.id.value.toString(),
                )

            headers.getFirst("code") shouldBe ErrorCode.AUTH_UNAUTHENTICATED.code
        }

        @Test
        fun `host connect is refused when authenticated user has no active game`() {
            val user = createTestUser()

            val headers =
                connectExpectingError(
                    port,
                    gameId = createTestGameId().value.toString(),
                    handshakeHeaders = buildHostHandshakeHeaders(user),
                )

            headers.getFirst("code") shouldBe ErrorCode.GAME_NOT_FOUND.code
        }

        @Test
        fun `host connect is refused when gameId is invalid`() {
            val user = testUserCreator.createTestUser()

            val headers =
                connectExpectingError(
                    port,
                    gameId = "invalid-game-id",
                    handshakeHeaders = buildHostHandshakeHeaders(user),
                )

            headers.getFirst("code") shouldBe ErrorCode.GAME_NOT_FOUND.code
        }

        @Test
        fun `host connect is refused when authenticated user is not the game host`() {
            val host = testUserCreator.createUniqueTestUser()
            val otherUser = testUserCreator.createUniqueTestUser()
            val session = testGameSessionCreator.createTestGameSession(host = host)

            val headers =
                connectExpectingError(
                    port,
                    gameId = session.id.value.toString(),
                    handshakeHeaders = buildHostHandshakeHeaders(otherUser),
                )

            headers.getFirst("code") shouldBe ErrorCode.GAME_NOT_FOUND.code
        }
    }

    private fun buildHostHandshakeHeaders(user: User): WebSocketHttpHeaders =
        WebSocketHttpHeaders().apply {
            val token = authTokenService.generateAccessToken(user)
            add(HttpHeaders.COOKIE, "${authConfiguration.cookie.accessTokenName}=${token.value}")
        }
}
