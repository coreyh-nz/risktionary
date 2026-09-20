package nz.coreyh.risktionary.feedback.domain.model

import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition

data class GamePlayerFeedbackAssignment(
    val framingCondition: FeedbackFramingCondition,
    val timingCondition: FeedbackTimingCondition,
)
