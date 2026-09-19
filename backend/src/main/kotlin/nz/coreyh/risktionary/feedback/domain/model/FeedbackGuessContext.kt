package nz.coreyh.risktionary.feedback.domain.model

import nz.coreyh.risktionary.game.domain.model.round.guess.GuessId


/**
 * A guess as seen by fact generation, together with the round context at the
 * moment it was made.
 *
 * @param drawingNote the AI's interpretation of the drawing that was current
 *    when the guess was made, or null if no meaningful analysis existed yet.
 * @param timeRemainingFraction how much of the drawing phase was left (1.0 at
 *    the start, 0.0 at the end), or null if the phase has no timer.
 */
data class FeedbackGuessContext(
    val guessId: GuessId,
    val text: String,
    val correct: Boolean,
    val drawingNote: String?,
    val timeRemainingFraction: Double?,
)
