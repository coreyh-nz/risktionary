package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

/** A player as recorded for research: no identity beyond [id]. */
data class PersistedGamePlayer(
    val id: GamePlayerId,
    val feedbackFramingCondition: FeedbackFramingCondition?,
    val feedbackTimingCondition: FeedbackTimingCondition?,
)
