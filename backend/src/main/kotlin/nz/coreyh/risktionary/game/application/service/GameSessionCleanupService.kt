package nz.coreyh.risktionary.game.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.config.GameSessionCleanupProperties
import nz.coreyh.risktionary.game.domain.model.GameStateType
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHostStatus
import org.springframework.stereotype.Service
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

private val kLogger = KotlinLogging.logger {}

@Service
class GameSessionCleanupService(
    private val gameSessionService: GameSessionService,
    private val gameSessionCleanupProperties: GameSessionCleanupProperties,
    private val clock: Clock = Clock.System,
) {
    fun cleanupStaleSessions() {
        val now = clock.now()
        val sessions = gameSessionService.getSessions()

        if (sessions.isEmpty()) {
            kLogger.debug { "Skipping game session cleanup as there are no sessions" }
            return
        }

        var removed = 0
        for (session in sessions) {
            val reason = session.staleReason(now)
            if (reason != null) {
                gameSessionService.removeSession(session.id)
                removed++
                kLogger.info { "Removed stale game session ${session.id}: $reason" }
            }
        }

        val message = "Game session cleanup complete - removed $removed/${sessions.size} sessions"
        if (removed > 0) kLogger.info { message } else kLogger.debug { message }
    }

    private fun GameSession.staleReason(now: Instant): String? =
        when (val hostStatus = host.status) {
            is GameSessionHostStatus.Pending -> {
                "host never connected since $createdAt"
                    .takeIf { isOlderThan(gameSessionCleanupProperties.hostJoinGracePeriod, now) }
            }

            is GameSessionHostStatus.Disconnected -> {
                "host abandoned at ${hostStatus.disconnectedAt}"
                    .takeIf { hostStatus.disconnectedAt + gameSessionCleanupProperties.hostAbandonedTimeout < now }
            }

            is GameSessionHostStatus.Connected -> {
                when {
                    isStarting() && isInactiveFor(gameSessionCleanupProperties.stuckStartingTimeout, now) -> {
                        "stuck in STARTING since $lastActivityAt"
                    }

                    else -> {
                        null
                    }
                }
            }
        }

    private fun GameSession.isStarting(): Boolean = state.type == GameStateType.STARTING

    private fun GameSession.isInactiveFor(
        duration: Duration,
        now: Instant,
    ): Boolean = lastActivityAt + duration < now

    private fun GameSession.isOlderThan(
        duration: Duration,
        now: Instant,
    ): Boolean = createdAt + duration < now
}
