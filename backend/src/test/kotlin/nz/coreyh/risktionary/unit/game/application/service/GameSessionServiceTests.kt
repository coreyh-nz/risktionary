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
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.service.GameSessionTaskService
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionChatService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.GameVolunteerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHostStatus
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayer
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSessionHostConnected
import nz.coreyh.risktionary.support.factory.game.createTestGameSessionHostPending
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.support.factory.word.createTestWord
import nz.coreyh.risktionary.words.application.service.WordService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@MockKTest
class GameSessionServiceTests {
    private lateinit var gameSessionTaskService: GameSessionTaskService
    private lateinit var gameTicketService: GameTicketService
    private lateinit var wordService: WordService
    private lateinit var gameRoundSessionService: GameRoundSessionService
    private lateinit var gameRoundSessionChatService: GameRoundSessionChatService
    private lateinit var gameSessionStore: GameSessionStore
    private lateinit var gameEventPublisher: GameEventPublisher
    private lateinit var clock: Clock

    private lateinit var service: GameSessionService

    @BeforeEach
    fun setup() {
        gameSessionTaskService = mockk(relaxed = true)
        gameTicketService = mockk(relaxed = true)
        wordService = mockk(relaxed = true)
        gameRoundSessionService = mockk(relaxed = true)
        gameRoundSessionChatService = mockk(relaxed = true)
        gameSessionStore = mockk(relaxed = true)
        gameEventPublisher = mockk(relaxed = true)
        clock = mockk(relaxed = true)
        service =
            GameSessionService(
                gameSessionTaskService,
                gameTicketService,
                wordService,
                gameRoundSessionService,
                gameRoundSessionChatService,
                gameSessionStore,
                gameEventPublisher,
                clock,
            )
    }

    @Nested
    @MockKTest
    inner class CreateSession {
        @Test
        fun `create session creates session with correct host`() {
            val hostId = createTestUserId()
            every { clock.now() } returns Clock.System.now()
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(hostId)

            session.host.id shouldBe hostId
            session.host.status.shouldBeInstanceOf<GameSessionHostStatus.Pending>()
        }

        @Test
        fun `create session creates session with valid code`() {
            every { clock.now() } returns Clock.System.now()
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(createTestUserId())

            session.id shouldNotBe null
            session.code.length shouldBe GameSessionService.CODE_LENGTH
        }

        @Test
        fun `create session creates session with current timestamp`() {
            val now = Clock.System.now()
            every { clock.now() } returns now
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(createTestUserId())

            session.createdAt shouldBe now
        }

        @Test
        fun `create session registers session in store`() {
            every { clock.now() } returns Clock.System.now()
            every { gameSessionStore.findByCode(any()) } returns null

            val session = service.createSession(createTestUserId())

            verify { gameSessionStore.addSession(session.id, session) }
        }

        @Test
        fun `create session throws when all code attempts collide`() {
            every { gameSessionStore.findByCode(any()) } returns mockk()

            shouldThrow<IllegalStateException> {
                service.createSession(createTestUserId())
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
        fun `transition to starting transitions session to starting state`() {
            val session = mockk<GameSession>(relaxed = true)
            val startIn = 10.seconds
            val startsAt = clock.now() + startIn
            every { gameSessionStore.findById(gameId) } returns session

            service.transitionToStarting(gameId)

            verify { session.transitionToStarting(startsAt) }
        }

        @Test
        fun `transition to starting publishes state event`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session

            service.transitionToStarting(gameId)

            verify { gameEventPublisher.publishState(session) }
        }

        @Test
        fun `transition to starting schedules transition to in progress`() {
            val session = mockk<GameSession>(relaxed = true)
            val now = Clock.System.now()
            val startIn = 10.seconds
            every { gameSessionStore.findById(gameId) } returns session
            every { clock.now() } returns now

            service.transitionToStarting(gameId)

            verify { gameSessionTaskService.schedule(gameId, now + startIn, any()) }
        }

        @Test
        fun `transition to starting throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.transitionToStarting(gameId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class TransitionToInProgress {
        private val gameId = createTestGameId()

        @Test
        fun `transitions session to in progress state`() {
            val session = mockk<GameSession>(relaxed = true)
            val word = createTestWord()
            every { gameSessionStore.findById(gameId) } returns session
            every { wordService.findWords() } returns listOf(word)

            service.transitionToInProgress(gameId)

            verify { session.transitionToInProgress() }
        }

        @Test
        fun `sets the current round`() {
            val session = mockk<GameSession>(relaxed = true)
            val roundSession = mockk<GameRoundSession>(relaxed = true)
            val word = createTestWord()
            every { gameSessionStore.findById(gameId) } returns session
            every { gameRoundSessionService.createRound(session, word) } returns roundSession
            every { wordService.findWords() } returns listOf(word)

            service.transitionToInProgress(gameId)

            verify { session.currentRound = roundSession }
        }

        @Test
        fun `publishes state event`() {
            val session = mockk<GameSession>(relaxed = true)
            val word = createTestWord()
            every { gameSessionStore.findById(gameId) } returns session
            every { wordService.findWords() } returns listOf(word)

            service.transitionToInProgress(gameId)

            verify { gameEventPublisher.publishState(session) }
        }

        @Test
        fun `throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.transitionToInProgress(gameId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleVolunteer {
        private val gameId = createTestGameId()
        private val playerId = createTestGamePlayerId()

        @Test
        fun `handle volunteer registers player as volunteer`() {
            val session = mockk<GameSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers

            service.handleVolunteer(gameId, playerId)

            verify { volunteers.volunteer(playerId) }
        }

        @Test
        fun `handle volunteer publishes updated volunteers list`() {
            val session = mockk<GameSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            val volunteerList = listOf(playerId)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers
            every { volunteers.getVolunteers() } returns volunteerList

            service.handleVolunteer(gameId, playerId)

            verify { gameEventPublisher.publishVolunteersUpdated(gameId, volunteerList) }
        }

        @Test
        fun `handle volunteer throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleVolunteer(gameId, playerId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleUnvolunteer {
        private val gameId = createTestGameId()
        private val playerId = createTestGamePlayerId()

        @Test
        fun `handle unvolunteer removes player volunteer nomination`() {
            val session = mockk<GameSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers

            service.handleUnvolunteer(gameId, playerId)

            verify { volunteers.unvolunteer(playerId) }
        }

        @Test
        fun `handle unvolunteer publishes updated volunteers list`() {
            val session = mockk<GameSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            val volunteerList = listOf<GamePlayerId>()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers
            every { volunteers.getVolunteers() } returns volunteerList

            service.handleUnvolunteer(gameId, playerId)

            verify { gameEventPublisher.publishVolunteersUpdated(gameId, volunteerList) }
        }

        @Test
        fun `handle unvolunteer throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleUnvolunteer(gameId, playerId)
            }
        }
    }

    @Nested
    @MockKTest
    inner class HandleSelectDrawer {
        private val gameId = createTestGameId()
        private val player = createTestGamePlayer()
        private val playerSession = createTestGamePlayerSession(player)

        @Test
        fun `handle select drawer selects the player as drawer on the active round`() {
            val session = mockk<GameSession>(relaxed = true)
            val round = mockk<GameRoundSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers
            every { session.getPlayer(player.id) } returns playerSession
            every { session.selectDrawer(playerSession) } returns round

            service.handleSelectDrawer(gameId, player.id)
        }

        @Test
        fun `handle select drawer publishes round state changed event`() {
            val session = mockk<GameSession>(relaxed = true)
            val round = mockk<GameRoundSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers
            every { session.getPlayer(player.id) } returns playerSession
            every { session.selectDrawer(playerSession) } returns round

            service.handleSelectDrawer(gameId, player.id)

            verify { gameEventPublisher.publishRoundState(round) }
        }

        @Test
        fun `handle select drawer publishes updated volunteers list after unvolunteering drawer`() {
            val session = mockk<GameSession>(relaxed = true)
            val round = mockk<GameRoundSession>(relaxed = true)
            val volunteers = mockk<GameVolunteerSession>(relaxed = true)
            val remainingVolunteers = listOf<GamePlayerId>()
            every { gameSessionStore.findById(gameId) } returns session
            every { session.volunteers } returns volunteers
            every { session.getPlayer(player.id) } returns playerSession
            every { session.selectDrawer(playerSession) } returns round
            every { volunteers.getVolunteers() } returns remainingVolunteers

            service.handleSelectDrawer(gameId, player.id)

            verify { gameEventPublisher.publishVolunteersUpdated(gameId, remainingVolunteers) }
        }

        @Test
        fun `handle select drawer throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.handleSelectDrawer(gameId, player.id)
            }
        }
    }
}
