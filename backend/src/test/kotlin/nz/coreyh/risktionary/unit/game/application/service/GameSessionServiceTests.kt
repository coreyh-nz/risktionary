package nz.coreyh.risktionary.unit.game.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerDisplayNameInUseException
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameStateOrchestrator
import nz.coreyh.risktionary.game.application.service.GameResearchPersistenceService
import nz.coreyh.risktionary.game.application.service.GameSessionFeedbackAssignmentService
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameSessionTaskService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.service.round.GameRoundDrawingAnalysisService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.GameVolunteerSession
import nz.coreyh.risktionary.game.application.session.GameWordsSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.config.GameScoringProperties
import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.TimeWindow
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHostStatus
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.factory.game.createTestCreateGameCommand
import nz.coreyh.risktionary.support.factory.game.createTestGameConfiguration
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayer
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSessionHostConnected
import nz.coreyh.risktionary.support.factory.game.createTestGameSessionHostPending
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.support.factory.word.createTestWord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

@MockKTest
class GameSessionServiceTests {
    private lateinit var gameSessionTaskService: GameSessionTaskService
    private lateinit var gameTicketService: GameTicketService
    private lateinit var gameRoundSessionService: GameRoundSessionService
    private lateinit var gameStateOrchestrator: GameStateOrchestrator
    private lateinit var gameRoundDrawingAnalysisService: GameRoundDrawingAnalysisService
    private lateinit var gameSessionFeedbackAssignmentService: GameSessionFeedbackAssignmentService
    private lateinit var gameResearchPersistenceService: GameResearchPersistenceService
    private lateinit var gameSessionStore: GameSessionStore
    private lateinit var gameEventPublisher: GameEventPublisher
    private lateinit var clock: Clock

    private lateinit var service: GameSessionService

    @BeforeEach
    fun setup() {
        gameSessionTaskService = mockk(relaxed = true)
        gameTicketService = mockk(relaxed = true)
        gameRoundSessionService = mockk(relaxed = true)
        gameStateOrchestrator = mockk(relaxed = true)
        gameRoundDrawingAnalysisService = mockk(relaxed = true)
        gameSessionFeedbackAssignmentService = mockk(relaxed = true)
        gameResearchPersistenceService = mockk(relaxed = true)
        gameSessionStore = mockk(relaxed = true)
        gameEventPublisher = mockk(relaxed = true)
        clock = mockk(relaxed = true)
        service =
            GameSessionService(
                gameSessionTaskService = gameSessionTaskService,
                gameTicketService = gameTicketService,
                gameRoundSessionService = gameRoundSessionService,
                gameStateOrchestrator = gameStateOrchestrator,
                gameRoundDrawingAnalysisService = gameRoundDrawingAnalysisService,
                gameSessionFeedbackAssignmentService = gameSessionFeedbackAssignmentService,
                gameResearchPersistenceService = gameResearchPersistenceService,
                gameScoringProperties = GameScoringProperties(1000, 100, java.time.Duration.ofSeconds(60)),
                gameSessionStore = gameSessionStore,
                gameEventPublisher = gameEventPublisher,
                clock = clock,
            )
    }

    @Nested
    @MockKTest
    inner class CreateSession {
        @Test
        fun `create session creates session with correct host`() {
            val hostId = createTestUserId()
            val command = createTestCreateGameCommand(hostId = hostId)
            every { clock.now() } returns Clock.System.now()
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(command)

            session.host.id shouldBe hostId
            session.host.status.shouldBeInstanceOf<GameSessionHostStatus.Pending>()
        }

        @Test
        fun `create session creates session with valid code`() {
            every { clock.now() } returns Clock.System.now()
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(createTestCreateGameCommand())

            session.id shouldNotBe null
            session.code.length shouldBe GameSessionService.CODE_LENGTH
        }

        @Test
        fun `create session creates session with current timestamp`() {
            val now = Clock.System.now()
            every { clock.now() } returns now
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(createTestCreateGameCommand())

            session.createdAt shouldBe now
        }

        @Test
        fun `create session registers session in store`() {
            every { clock.now() } returns Clock.System.now()
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(createTestCreateGameCommand())

            verify { gameSessionStore.addSession(session.id, session) }
        }

        @Test
        fun `create session throws when all code attempts collide`() {
            every { gameSessionStore.findByCode(any()) } returns mockk()

            shouldThrow<IllegalStateException> {
                service.createSession(createTestCreateGameCommand())
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleJoinRequest {
        private val code = "123456"
        private val identity = createTestGamePlayerIdentityGuest()
        private val gameId = createTestGameId()

        @Test
        fun `handle join request returns ticket when session exists`() {
            val session = mockk<GameSession>(relaxed = true)
            val ticket = mockk<GameTicket>()
            every { gameSessionStore.findByCode(code) } returns session
            every { gameTicketService.generateTicket(any(), any()) } returns ticket

            val result = service.handleJoinRequest(code, identity)

            result shouldBe ticket
        }

        @Test
        fun `handle join request registers player join request on session`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findByCode(code) } returns session

            service.handleJoinRequest(code, identity)

            verify { session.requestJoin(any<GamePlayerSession>()) }
        }

        @Test
        fun `handle join request generates ticket for the correct game`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findByCode(code) } returns session
            every { session.id } returns gameId

            service.handleJoinRequest(code, identity)

            verify { gameTicketService.generateTicket(gameId, any()) }
        }

        @Test
        fun `handle join request throws when display name is already in use`() {
            val session = mockk<GameSession>(relaxed = true)
            val existingPlayer = createTestGamePlayer(identity = identity)
            val existingPlayerSession = createTestGamePlayerSession(player = existingPlayer)
            every { gameSessionStore.findByCode(code) } returns session
            every { session.getPlayers() } returns listOf(existingPlayerSession)

            shouldThrow<GamePlayerDisplayNameInUseException> {
                service.handleJoinRequest(code, identity)
            }
        }

        @Test
        fun `handle join request throws when session does not exist`() {
            every { gameSessionStore.findByCode(code) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleJoinRequest(code, identity)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleConnecting {
        private val gameId = createTestGameId()
        private val playerId = createTestGamePlayerId()

        @Test
        fun `handle connecting connects player to session`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session

            service.handleConnecting(gameId, playerId)

            verify { session.connect(playerId) }
        }

        @Test
        fun `handle connecting throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleConnecting(gameId, playerId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleConnected {
        private val gameId = createTestGameId()
        private val playerId = createTestGamePlayerId()

        @Test
        fun `handle connected activates player in session`() {
            val session = mockk<GameSession>(relaxed = true)
            val player = mockk<GamePlayerSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.activate(playerId) } returns player

            service.handleConnected(gameId, playerId)

            verify { session.activate(playerId) }
        }

        @Test
        fun `handle connected publishes player joined event`() {
            val session = mockk<GameSession>(relaxed = true)
            val player = mockk<GamePlayerSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.activate(playerId) } returns player

            service.handleConnected(gameId, playerId)

            verify { gameEventPublisher.publishPlayerJoined(gameId, player) }
        }

        @Test
        fun `handle connected throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleConnected(gameId, playerId)
            }
        }

        @Test
        fun `handle connected assigns feedback combination when feedback generation is enabled`() {
            val session = mockk<GameSession>(relaxed = true)
            val player = mockk<GamePlayerSession>(relaxed = true)
            val config = mockk<GameConfiguration>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.activate(playerId) } returns player
            every { session.config } returns config
            every { config.feedbackGenerationEnabled } returns true

            service.handleConnected(gameId, playerId)

            verify { gameSessionFeedbackAssignmentService.assign(session, player) }
        }

        @Test
        fun `handle connected does not assign feedback combination when feedback generation is disabled`() {
            val session = mockk<GameSession>(relaxed = true)
            val player = mockk<GamePlayerSession>(relaxed = true)
            val config = mockk<GameConfiguration>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.activate(playerId) } returns player
            every { session.config } returns config
            every { config.feedbackGenerationEnabled } returns false

            service.handleConnected(gameId, playerId)

            verify(exactly = 0) { gameSessionFeedbackAssignmentService.assign(any(), any()) }
        }
    }

    @Nested
    @MockKTest
    inner class HandleReady {
        private val playerId = createTestGamePlayerId()

        @Test
        fun `handle ready publishes player list to the connecting player`() {
            val session = mockk<GameSession>(relaxed = true)
            val players = listOf(mockk<GamePlayerSession>())
            every { gameSessionStore.findByPlayerId(playerId) } returns session
            every { session.getPlayers() } returns players
            service.handleReady(playerId)

            verify { gameEventPublisher.publishPlayerList(playerId, players) }
        }

        @Test
        fun `handle ready throws when session does not exist`() {
            every { gameSessionStore.findByPlayerId(playerId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleReady(playerId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleDisconnected {
        private val gameId = createTestGameId()
        private val playerId = createTestGamePlayerId()

        @Test
        fun `handle disconnected disconnects player from session`() {
            val session = mockk<GameSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers

            service.handleDisconnected(gameId, playerId)

            verify { session.disconnect(playerId) }
        }

        @Test
        fun `handle disconnected publishes player left event`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session

            service.handleDisconnected(gameId, playerId)

            verify { gameEventPublisher.publishPlayerLeft(gameId, playerId) }
        }

        @Test
        fun `handle disconnected removes player from volunteers`() {
            val session = mockk<GameSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            val remainingVolunteers = listOf(createTestGamePlayerId())
            every { session.volunteers } returns volunteers
            every { gameSessionStore.findById(gameId) } returns session
            every { volunteers.getVolunteers() } returns remainingVolunteers

            service.handleDisconnected(gameId, playerId)

            verify { volunteers.removeIfPresent(playerId) }
        }

        @Test
        fun `handle disconnected publishes updated volunteers list after player removed`() {
            val session = mockk<GameSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            val remainingVolunteers = listOf<GamePlayerId>()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers
            every { volunteers.getVolunteers() } returns remainingVolunteers

            service.handleDisconnected(gameId, playerId)

            verify { gameEventPublisher.publishVolunteersUpdated(gameId, remainingVolunteers) }
        }

        @Test
        fun `handle disconnected throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleDisconnected(gameId, playerId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleHostConnected {
        private val hostId = createTestUserId()
        private val gameId = createTestGameId()

        @Test
        fun `handle host connected connects host to session`() {
            val session = mockk<GameSession>(relaxed = true)
            val host = createTestGameSessionHostPending(id = hostId)
            val now = Clock.System.now()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.host } returns host
            every { clock.now() } returns now

            service.handleHostConnected(hostId, gameId)

            host.status.shouldBeTypeOf<GameSessionHostStatus.Connected> {
                it.connectedAt shouldBe now
            }
        }

        @Test
        fun `handle host connected throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleHostConnected(hostId, gameId)
            }
        }

        @Test
        fun `handle host connected throws when host id does not match session host`() {
            val session = mockk<GameSession>(relaxed = true)
            val host = createTestGameSessionHostPending()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.host } returns host

            shouldThrow<GameNotFoundException> {
                service.handleHostConnected(hostId, gameId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleHostDisconnected {
        private val hostId = createTestUserId()
        private val gameId = createTestGameId()

        @Test
        fun `handle host disconnected disconnects host from session`() {
            val session = mockk<GameSession>(relaxed = true)
            val host = createTestGameSessionHostConnected(id = hostId)
            val now = Clock.System.now()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.host } returns host
            every { clock.now() } returns now

            service.handleHostDisconnected(hostId, gameId)

            host.status.shouldBeTypeOf<GameSessionHostStatus.Disconnected> {
                it.disconnectedAt shouldBe now
            }
        }

        @Test
        fun `handle host disconnected throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleHostDisconnected(hostId, gameId)
            }
        }

        @Test
        fun `handle host disconnected throws when host id does not match session host`() {
            val session = mockk<GameSession>(relaxed = true)
            val host = createTestGameSessionHostConnected()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.host } returns host

            shouldThrow<GameNotFoundException> {
                service.handleHostDisconnected(hostId, gameId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class TransitionToStarting {
        private val gameId = createTestGameId()

        @Test
        fun `transition to starting delegates to game state orchestrator with lobby countdown time window`() {
            val session = mockk<GameSession>(relaxed = true)
            val wordsSession = mockk<GameWordsSession>()
            val gameConfig = createTestGameConfiguration()
            val timeWindow = TimeWindow(startedAt = clock.now(), duration = gameConfig.lobbyCountdown)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.config } returns gameConfig
            every { session.words } returns wordsSession
            every { wordsSession.nextWord() } returns createTestWord()

            service.transitionToStarting(gameId)

            verify { gameStateOrchestrator.transitionToStarting(session, timeWindow) }
        }

        @Test
        fun `transition to starting throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> { service.transitionToStarting(gameId) }

            verify(exactly = 0) {
                gameStateOrchestrator.transitionToStarting(any(), any())
            }
        }
    }
}
