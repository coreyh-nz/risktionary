package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.user.domain.model.UserId
import kotlin.time.Instant

/**
 * A game listed among all persisted games, for picking one to look up in
 * full via [nz.coreyh.risktionary.game.application.service.GameDetailsService.getDetails].
 */
data class PersistedGameSummary(
    val id: GameId,
    val code: String,
    val hostUserId: UserId,
    val createdAt: Instant,
    val feedbackGenerationMode: FeedbackGenerationMode,
    val endReason: GameEndReason?,
    val endedAt: Instant?,
)
