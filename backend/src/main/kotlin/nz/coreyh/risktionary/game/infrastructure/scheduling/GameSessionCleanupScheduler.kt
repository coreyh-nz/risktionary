package nz.coreyh.risktionary.game.infrastructure.scheduling

import nz.coreyh.risktionary.game.application.service.GameSessionCleanupService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class GameSessionCleanupScheduler(
    private val gameSessionCleanupService: GameSessionCleanupService,
) {
    @Scheduled(fixedDelayString = $$"${app.game.session.cleanup.interval}")
    fun run() = gameSessionCleanupService.cleanupStaleSessions()
}
