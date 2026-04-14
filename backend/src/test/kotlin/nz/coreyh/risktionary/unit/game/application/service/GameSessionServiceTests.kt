package nz.coreyh.risktionary.unit.game.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GameTicketInvalidException
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.factory.game.createTestGameTicket
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.BadJwtException

class GameSessionServiceTests {
    private lateinit var gameTicketService: GameTicketService
    private lateinit var gameSessionStore: GameSessionStore
    private lateinit var service: GameSessionService

    @BeforeEach
    fun setup() {
        gameTicketService = mockk()
        gameSessionStore = mockk()
        service = GameSessionService(gameTicketService, gameSessionStore)
    }

    @Test
    fun `create session creates session when host id is provided`() {
        val hostId = createTestUserId()
        every { gameSessionStore.findByCode(any()) } returns null
        every { gameSessionStore.addSession(any(), any()) } just Runs

        val session = service.createSession(hostId)

        session.hostId shouldBe hostId
        session.id shouldNotBe null
        session.code.length shouldBe GameSessionService.CODE_LENGTH
        verify { gameSessionStore.addSession(session.id, session) }
    }

    @Test
    fun `handle join request returns ticket when session exists`() {
        val code = "123456"
        val gameId = createTestGameId()
        val playerId = createTestGamePlayerId()
        val session = mockk<GameSession>()
        val identity = createTestGamePlayerIdentityGuest()
        val ticket = createTestGameTicket(gameId = gameId, playerId = playerId, value = "ticket12345")

        every { gameSessionStore.findByCode(code) } returns session
        every { session.id } returns gameId
        every { session.requestJoin(any()) } just Runs
        every { gameTicketService.generateTicket(any(), any()) } returns ticket

        val result = service.handleJoinRequest(code, identity)

        result shouldBe ticket
        verify { session.requestJoin(any<GamePlayerSession>()) }
    }

    @Test
    fun `handle join request throws when session does not exist`() {
        val code = "123456"
        val identity = createTestGamePlayerIdentityGuest()
        every { gameSessionStore.findByCode(code) } returns null

        shouldThrow<GameNotFoundException> {
            service.handleJoinRequest(code, identity)
        }
    }

    @Test
    fun `handle connecting connects player when ticket is valid`() {
        val ticket = createTestGameTicket()
        val session = mockk<GameSession>()
        every { gameTicketService.decodeTicket(ticket.value) } returns ticket
        every { gameSessionStore.findById(ticket.gameId) } returns session
        every { session.connect(ticket.playerId) } just Runs

        service.handleConnecting(ticket.value)

        verify { session.connect(ticket.playerId) }
    }

    @Test
    fun `handle connecting throws when ticket is invalid`() {
        val ticketValue = "invalid"
        every { gameTicketService.decodeTicket(ticketValue) } throws BadJwtException("")

        shouldThrow<GameTicketInvalidException> {
            service.handleConnecting(ticketValue)
        }
    }

    @Test
    fun `handle connected activates player when session exists`() {
        val session = mockk<GameSession>()
        val gameId = createTestGameId()
        val playerId = createTestGamePlayerId()
        every { gameSessionStore.findById(gameId) } returns session
        every { session.activate(playerId) } just Runs

        service.handleConnected(gameId, playerId)

        verify { session.activate(playerId) }
    }

    @Test
    fun `handle connected throws when session does not exist`() {
        val gameId = createTestGameId()
        val playerId = createTestGamePlayerId()

        every { gameSessionStore.findById(gameId) } returns null

        shouldThrow<GameNotFoundException> {
            service.handleConnected(gameId, playerId)
        }
    }
}
