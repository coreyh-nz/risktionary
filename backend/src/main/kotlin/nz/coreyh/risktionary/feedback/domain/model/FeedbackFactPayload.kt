package nz.coreyh.risktionary.feedback.domain.model

import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.words.domain.model.Word

/**
 * Everything needed to generate one piece of feedback.
 *
 * [guesses] are all the guesses the feedback should take into account, in
 * the order they were made. They are treated as one evolving attempt.
 *
 * [timing] decides which prompts are used: instant feedback is a short hint
 * that never reveals the answer, delayed feedback is a round debrief written
 * after the answer is known.
 */
data class FeedbackFactPayload(
    val guesses: List<FeedbackGuessContext>,
    val word: Word,
    val condition: FeedbackFramingCondition,
    val timing: FeedbackTimingCondition,
) {
    init {
        require(guesses.isNotEmpty()) { "Feedback needs at least one guess" }
    }
}
