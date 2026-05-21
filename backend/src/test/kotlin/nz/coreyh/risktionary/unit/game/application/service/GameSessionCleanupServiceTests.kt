package nz.coreyh.risktionary.unit.game.application.service

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.application.service.GameSessionCleanupService
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.config.GameSessionCleanupProperties
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.factory.game.createTestGameSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSessionHostConnected
import nz.coreyh.risktionary.support.factory.game.createTestGameSessionHostDisconnected
import nz.coreyh.risktionary.support.factory.game.createTestGameSessionHostPending
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@MockKTest
class GameSessionCleanupServiceTests {
    private lateinit var gameSessionService: GameSessionService
    private lateinit var clock: Clock
    private val cleanupProperties =
        GameSessionCleanupProperties(
            hostJoinGracePeriod = 5.minutes.toJavaDuration(),
            hostAbandonedTimeout = 10.minutes.toJavaDuration(),
            stuckStartingTimeout = 15.minutes.toJavaDuration(),
        )

    private lateinit var gameSessionCleanupService: GameSessionCleanupService

    @BeforeEach
    fun setup() {
        gameSessionService = mockk()
        clock = mockk()
        gameSessionCleanupService =
            GameSessionCleanupService(
                gameSessionService = gameSessionService,
                gameSessionCleanupProperties = cleanupProperties,
                clock = clock,
            )
    }

    @Test
    fun `cleanup stale sessions does not remove any sessions when there are no sessions`() {
        val now = Clock.System.now()
        every { clock.now() } returns now
        every { gameSessionService.getSessions() } returns emptyList()

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 0) { gameSessionService.removeSession(any()) }
    }

    @Test
    fun `cleanup stale sessions removes session when host is pending and grace period has elapsed`() {
        val now = Clock.System.now()
        every { clock.now() } returns now

        val session =
            createTestGameSession(
                host = createTestGameSessionHostPending(),
                createdAt = now - cleanupProperties.hostJoinGracePeriod - 1.minutes,
            )
        every { gameSessionService.getSessions() } returns listOf(session)
        justRun { gameSessionService.removeSession(session.id) }

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 1) { gameSessionService.removeSession(session.id) }
    }

    @Test
    fun `cleanup stale sessions does not remove session when host is pending and grace period has not elapsed`() {
        val now = Clock.System.now()
        every { clock.now() } returns now

        val session =
            createTestGameSession(
                host = createTestGameSessionHostPending(),
                createdAt = now - cleanupProperties.hostJoinGracePeriod + 1.minutes,
            )
        every { gameSessionService.getSessions() } returns listOf(session)

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 0) { gameSessionService.removeSession(any()) }
    }

    @Test
    fun `cleanup stale sessions removes session when host is disconnected and abandoned timeout has elapsed`() {
        val now = Clock.System.now()
        every { clock.now() } returns now

        val disconnectedAt = now - cleanupProperties.hostAbandonedTimeout - 1.minutes
        val session =
            createTestGameSession(
                host = createTestGameSessionHostDisconnected(disconnectedAt = disconnectedAt),
                createdAt = now - 30.minutes,
            )
        every { gameSessionService.getSessions() } returns listOf(session)
        justRun { gameSessionService.removeSession(session.id) }

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 1) { gameSessionService.removeSession(session.id) }
    }

    @Test
    fun `cleanup stale sessions does not remove session when host is disconnected and abandoned timeout has not elapsed`() {
        val now = Clock.System.now()
        val disconnectedAt = now - cleanupProperties.hostAbandonedTimeout + 1.minutes
        val session =
            createTestGameSession(
                host = createTestGameSessionHostDisconnected(disconnectedAt = disconnectedAt),
                createdAt = now - 30.minutes,
            )
        every { clock.now() } returns now
        every { gameSessionService.getSessions() } returns listOf(session)

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 0) { gameSessionService.removeSession(any()) }
    }

    @Test
    fun `cleanup stale sessions removes session when host is connected and session is stuck in starting and stuck starting timeout has elapsed`() {
        val now = Clock.System.now()
        val sessionClock =
            mockk<Clock> {
                every { now() } returns now - cleanupProperties.stuckStartingTimeout - 1.minutes
            }
        every { clock.now() } returns now

        val session =
            createTestGameSession(
                host = createTestGameSessionHostConnected(connectedAt = now - 30.minutes),
                createdAt = now - 30.minutes,
                clock = sessionClock,
            ).also { it.transitionToStarting(10.seconds) }

        every { gameSessionService.getSessions() } returns listOf(session)
        justRun { gameSessionService.removeSession(session.id) }

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 1) { gameSessionService.removeSession(session.id) }
    }

    @Test
    fun `cleanup stale sessions does not remove session when host is connected and session is stuck in starting and stuck starting timeout has not elapsed`() {
        val now = Clock.System.now()
        val sessionClock =
            mockk<Clock> {
                every { now() } returns now - cleanupProperties.stuckStartingTimeout + 1.minutes
            }
        every { clock.now() } returns now

        val session =
            createTestGameSession(
                host = createTestGameSessionHostConnected(connectedAt = now - 30.minutes),
                createdAt = now - 30.minutes,
                clock = sessionClock,
            ).also { it.transitionToStarting(10.seconds) }
        every { gameSessionService.getSessions() } returns listOf(session)

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 0) { gameSessionService.removeSession(any()) }
    }

    @Test
    fun `cleanup stale sessions does not remove session when host is connected and session is not in starting state`() {
        val now = Clock.System.now()
        val sessionClock = mockk<Clock>()
        every { clock.now() } returns now

        val session =
            createTestGameSession(
                host = createTestGameSessionHostConnected(connectedAt = now - 30.minutes),
                createdAt = now - 30.minutes,
                clock = sessionClock,
            )

        every { gameSessionService.getSessions() } returns listOf(session)

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 0) { gameSessionService.removeSession(any()) }
    }

    @Test
    fun `cleanup stale sessions removes only stale sessions when sessions contain a mix of stale and active sessions`() {
        val now = Clock.System.now()
        every { clock.now() } returns now

        val staleSession =
            createTestGameSession(
                host = createTestGameSessionHostPending(),
                createdAt = now - cleanupProperties.hostJoinGracePeriod - 1.minutes,
            )
        val activeSession =
            createTestGameSession(
                host = createTestGameSessionHostConnected(connectedAt = now - 1.minutes),
                createdAt = now - 1.minutes,
            )

        every { gameSessionService.getSessions() } returns listOf(staleSession, activeSession)
        justRun { gameSessionService.removeSession(staleSession.id) }

        gameSessionCleanupService.cleanupStaleSessions()

        verify(exactly = 1) { gameSessionService.removeSession(staleSession.id) }
        verify(exactly = 0) { gameSessionService.removeSession(activeSession.id) }
    }
}
