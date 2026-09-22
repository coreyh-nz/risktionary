package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.FeedbackId
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessId
import kotlin.time.Instant

data class PersistedFeedback(
    val id: FeedbackId,
    val playerId: GamePlayerId,
    val framingCondition: FeedbackFramingCondition,
    val timingCondition: FeedbackTimingCondition,
    val status: FeedbackGenerationStatus,
    val factText: String?,
    val framedText: String?,
    val generatedAt: Instant,
    /** The guess (instant feedback) or guesses (delayed feedback) this was generated from. */
    val sourceGuessIds: List<GuessId>,
)
