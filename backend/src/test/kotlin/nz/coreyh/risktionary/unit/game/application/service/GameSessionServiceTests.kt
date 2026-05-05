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
import nz.coreyh.risktionary.game.application.exception.GamePlayerDisplayNameInUseException
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameSessionTaskService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class GameSessionServiceTests {
    private lateinit var gameSessionTaskService: GameSessionTaskService
    private lateinit var gameTicketService: GameTicketService
    private lateinit var gameSessionStore: GameSessionStore
    private lateinit var gameEventPublisher: GameEventPublisher
    private lateinit var clock: Clock
    private lateinit var service: GameSessionService

    @BeforeEach
    fun setup() {
        gameSessionTaskService = mockk()
        gameTicketService = mockk()
        gameSessionStore = mockk()
        gameEventPublisher = mockk()
        clock = mockk()
        service =
            GameSessionService(
                gameSessionTaskService,
                gameTicketService,
                gameSessionStore,
                gameEventPublisher,
                clock,
            )
    }

    @Nested
    inner class Create {
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
        fun `create session throws when all code attempts produce a collision`() {
            val hostId = createTestUserId()
            every { gameSessionStore.findByCode(any()) } returns mockk()

            assertThrows<IllegalStateException> {
                service.createSession(hostId)
            }
        }
    }

    @Nested
    inner class HandleJoinRequest {
        @Test
        fun `handle join request returns ticket when session exists`() {
            val code = "123456"
            val gameId = createTestGameId()
            val identity = createTestGamePlayerIdentityGuest()
            val session = mockk<GameSession>()
            val ticket = mockk<GameTicket>()
            every { gameSessionStore.findByCode(code) } returns session
            every { session.id } returns gameId
            every { session.getPlayers() } returns emptyList()
            every { session.requestJoin(any()) } just Runs
            every { gameTicketService.generateTicket(any(), any()) } returns ticket

            val result = service.handleJoinRequest(code, identity)

            result shouldBe ticket
            verify { session.requestJoin(any<GamePlayerSession>()) }
            verify { gameTicketService.generateTicket(gameId, any()) }
        }

        @Test
        fun `handle join request throws when display name is already in use`() {
            val code = "123456"
            val identity = createTestGamePlayerIdentityGuest()
            val session = mockk<GameSession>()
            val existingPlayer = createTestGamePlayerSession(identity = identity)

            every { gameSessionStore.findByCode(code) } returns session
            every { session.id } returns createTestGameId()
            every { session.getPlayers() } returns listOf(existingPlayer)

            shouldThrow<GamePlayerDisplayNameInUseException> {
                service.handleJoinRequest(code, identity)
            }
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
    }

    @Nested
    inner class HandleConnecting {
        @Test
        fun `handle connecting connects player when session exists`() {
            val gameId = createTestGameId()
            val playerId = createTestGamePlayerId()
            val session = mockk<GameSession>()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.connect(playerId) } just Runs

            service.handleConnecting(gameId, playerId)

            verify { session.connect(playerId) }
        }

        @Test
        fun `handle connecting throws when session does not exist`() {
            val gameId = createTestGameId()
            val playerId = createTestGamePlayerId()
            every { gameSessionStore.findById(gameId) } returns null

            assertThrows<GameNotFoundException> {
                service.handleConnecting(gameId, playerId)
            }
        }
    }

    @Nested
    inner class HandleConnected {
        @Test
        fun `handle connected activates player when session exists`() {
            val gameId = createTestGameId()
            val playerId = createTestGamePlayerId()
            val session = mockk<GameSession>()
            val player = mockk<GamePlayerSession>()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.activate(playerId) } returns player
            every { gameEventPublisher.publishPlayerJoined(gameId, player) } just Runs

            service.handleConnected(gameId, playerId)

            verify { session.activate(playerId) }
            verify { gameEventPublisher.publishPlayerJoined(gameId, player) }
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

    @Nested
    inner class HandleReady {
        @Test
        fun `handle ready publishes player list when session exists`() {
            val playerId = createTestGamePlayerId()
            val session = mockk<GameSession>()
            val players = listOf(mockk<GamePlayerSession>())

            every { gameSessionStore.findByPlayerId(playerId) } returns session
            every { session.getPlayers() } returns players
            every { gameEventPublisher.publishPlayerList(playerId, players) } just Runs

            service.handleReady(playerId)

            verify { gameEventPublisher.publishPlayerList(playerId, players) }
        }

        @Test
        fun `handle ready throws when session does not exist`() {
            val playerId = createTestGamePlayerId()

            every { gameSessionStore.findByPlayerId(playerId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleReady(playerId)
            }
        }
    }

    @Nested
    inner class HandleDisconnected {
        @Test
        fun `handle disconnected disconnects player and publishes event when session exists`() {
            val gameId = createTestGameId()
            val playerId = createTestGamePlayerId()
            val session = mockk<GameSession>()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.disconnect(playerId) } just Runs
            every { gameEventPublisher.publishPlayerLeft(gameId, playerId) } just Runs

            service.handleDisconnected(gameId, playerId)

            verify { session.disconnect(playerId) }
            verify { gameEventPublisher.publishPlayerLeft(gameId, playerId) }
        }

        @Test
        fun `handle disconnected throws when session does not exist`() {
            val gameId = createTestGameId()
            val playerId = createTestGamePlayerId()
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleDisconnected(gameId, playerId)
            }
        }
    }

    @Nested
    inner class TransitionToStarting {
        @Test
        fun `transition to starting updates session state publishes event and schedules task`() {
            val gameId = createTestGameId()
            val session = mockk<GameSession>()
            val gameState = GameState.Lobby
            val now = Clock.System.now()
            val expectedStart = now + 10.seconds // todo - change this to use time from settings when implemented
            every { gameSessionStore.findById(gameId) } returns session
            every { gameEventPublisher.publishStateChanged(gameId, gameState) } just Runs
            every { gameSessionTaskService.schedule(gameId, expectedStart, any()) } just Runs
            every { session.state } returns gameState
            every { session.transitionToStarting(expectedStart) } just Runs
            every { clock.now() } returns now

            service.transitionToStarting(gameId)

            verify { session.transitionToStarting(expectedStart) }
            verify { gameEventPublisher.publishStateChanged(gameId, gameState) }
            verify { gameSessionTaskService.schedule(gameId, expectedStart, any()) }
        }

        @Test
        fun `transition to starting throws when session does not exist`() {
            val gameId = createTestGameId()
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.transitionToStarting(gameId)
            }
        }
    }

    @Nested
    inner class TransitionToInProgress {
        @Test
        fun `transition to in progress updates session state publishes event`() {
            val gameId = createTestGameId()
            val session = mockk<GameSession>()
            val gameState = GameState.InProgress
            every { gameSessionStore.findById(gameId) } returns session
            every { gameEventPublisher.publishStateChanged(gameId, gameState) } just Runs
            every { session.state } returns gameState
            every { session.transitionToInProgress() } just Runs

            service.transitionToInProgress(gameId)

            verify { session.transitionToInProgress() }
            verify { gameEventPublisher.publishStateChanged(gameId, gameState) }
        }

        @Test
        fun `transition to starting throws when session does not exist`() {
            val gameId = createTestGameId()
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.transitionToInProgress(gameId)
            }
        }
    }
}
