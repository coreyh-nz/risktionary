package nz.coreyh.risktionary.unit.game.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nz.coreyh.risktionary.game.application.exception.GameTicketInvalidException
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.config.TicketProperties
import nz.coreyh.risktionary.shared.application.service.TokenService
import nz.coreyh.risktionary.shared.domain.model.Token
import nz.coreyh.risktionary.shared.exception.UnauthenticatedException
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.BadJwtException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class GameTicketServiceTests {
    private lateinit var tokenService: TokenService
    private lateinit var ticketProperties: TicketProperties
    private lateinit var clock: Clock
    private lateinit var service: GameTicketService

    @BeforeEach
    fun setup() {
        tokenService = mockk()
        ticketProperties = TicketProperties(lifetime = 10.minutes.toJavaDuration())
        clock = mockk()
        service = GameTicketService(tokenService, ticketProperties, clock)
    }

    @Test
    fun `generate ticket returns ticket when inputs are valid`() {
        val now = Clock.System.now()
        val gameId = createTestGameId()
        val playerId = createTestGamePlayerId()
        val token =
            Token(
                subject = playerId.toString(),
                issuedAt = now,
                expiresAt = now + ticketProperties.lifetime,
                value = "token-value",
                claims = mapOf(GameTicketService.GAME_CLAIM to gameId.toString()),
            )
        every { clock.now() } returns now
        every {
            tokenService.generateToken(
                subject = token.subject,
                issuedAt = token.issuedAt,
                expiresAt = token.expiresAt,
                type = GameTicketService.TOKEN_TYPE,
                claims = mapOf(GameTicketService.GAME_CLAIM to gameId.toString()),
            )
        } returns token

        val result = service.generateTicket(gameId, playerId)

        result.gameId shouldBe gameId
        result.playerId shouldBe playerId
        result.issuedAt shouldBe token.issuedAt
        result.expiresAt shouldBe token.expiresAt
        result.value shouldBe token.value
    }

    @Test
    fun `decode ticket returns ticket when token is valid`() {
        val issuedAt = Clock.System.now() - 10.minutes
        val gameId = createTestGameId()
        val playerId = createTestGamePlayerId()
        val token =
            Token(
                subject = playerId.toString(),
                issuedAt = issuedAt,
                expiresAt = issuedAt + ticketProperties.lifetime,
                value = "token-value",
                claims = mapOf(GameTicketService.GAME_CLAIM to gameId.toString()),
            )
        every { tokenService.decodeToken(token.value, GameTicketService.TOKEN_TYPE) } returns token

        val result = service.decodeTicket(token.value)

        result.gameId shouldBe gameId
        result.playerId shouldBe playerId
        result.issuedAt shouldBe token.issuedAt
        result.expiresAt shouldBe token.expiresAt
        result.value shouldBe token.value
    }

    @Test
    fun `decode ticket throws when token is invalid jwt`() {
        val tokenValue = "invalid"
        every { tokenService.decodeToken(tokenValue, GameTicketService.TOKEN_TYPE) } throws BadJwtException("")

        shouldThrow<UnauthenticatedException> {
            service.decodeTicket(tokenValue)
        }
    }

    @Test
    fun `decode ticket throws when subject is not a player id`() {
        val now = Clock.System.now()
        val gameId = createTestGameId()
        val token =
            Token(
                subject = "invalid",
                issuedAt = now,
                expiresAt = now + ticketProperties.lifetime,
                value = "token-value",
                claims = mapOf(GameTicketService.GAME_CLAIM to gameId.toString()),
            )
        every { tokenService.decodeToken(token.value, GameTicketService.TOKEN_TYPE) } returns token

        shouldThrow<GameTicketInvalidException> {
            service.decodeTicket(token.value)
        }
    }

    @Test
    fun `decode ticket throws when game claim is missing`() {
        val now = Clock.System.now()
        val playerId = createTestGamePlayerId()
        val token =
            Token(
                subject = playerId.value.toString(),
                issuedAt = now,
                expiresAt = now + ticketProperties.lifetime,
                value = "token-value",
                claims = mapOf(),
            )
        every { tokenService.decodeToken(token.value, GameTicketService.TOKEN_TYPE) } returns token

        shouldThrow<GameTicketInvalidException> {
            service.decodeTicket(token.value)
        }
    }

    @Test
    fun `decode ticket throws when game claim is not a game id`() {
        val now = Clock.System.now()
        val playerId = createTestGamePlayerId()
        val token =
            Token(
                subject = playerId.value.toString(),
                issuedAt = now,
                expiresAt = now + ticketProperties.lifetime,
                value = "token-value",
                claims = mapOf(GameTicketService.GAME_CLAIM to "invalid"),
            )
        every { tokenService.decodeToken(token.value, GameTicketService.TOKEN_TYPE) } returns token

        shouldThrow<GameTicketInvalidException> {
            service.decodeTicket(token.value)
        }
    }
}
