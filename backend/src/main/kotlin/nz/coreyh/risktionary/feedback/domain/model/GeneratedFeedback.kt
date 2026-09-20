package nz.coreyh.risktionary.feedback.domain.model

import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessId
import kotlin.time.Instant

/**
 * Feedback generated for a player during a round, together with the guess (or
 * guesses, for delayed feedback) it was generated from.
 */
data class GeneratedFeedback(
    val id: FeedbackId,
    val playerId: GamePlayerId,
    val sourceGuessIds: List<GuessId>,
    val framingCondition: FeedbackFramingCondition,
    val timingCondition: FeedbackTimingCondition,
    val status: FeedbackGenerationStatus,
    val factText: String?,
    val framedText: String?,
    val generatedAt: Instant,
)
