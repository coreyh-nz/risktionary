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
 *
 * [guessedCorrectly] is whether the player reached the correct answer. Correct
 * guesses are not part of [guesses], so this is the only way the debrief knows
 * how the round ended for the player. It is always false for instant feedback,
 * which is generated while the player is still guessing.
 *
 * [previousFacts] are the fact notes already generated for this player earlier in
 * the round, oldest first, so fact generation can avoid repeating itself across
 * successive instant-feedback calls. Empty for delayed feedback, which is only
 * generated once.
 */
data class FeedbackFactPayload(
    val guesses: List<FeedbackGuessContext>,
    val word: Word,
    val condition: FeedbackFramingCondition,
    val timing: FeedbackTimingCondition,
    val guessedCorrectly: Boolean = false,
    val previousFacts: List<String> = emptyList(),
) {
    init {
        require(guesses.isNotEmpty()) { "Feedback needs at least one guess" }
    }
}
