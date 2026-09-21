package nz.coreyh.risktionary.game.domain.model.scoring

import kotlin.time.Duration

/**
 * How points are awarded for a correct guess.
 *
 * A guess made at the very start of the drawing phase earns [maxPoints]. Points
 * fall linearly to [minPoints] as the phase runs out, so a guess is worth the
 * same whatever the phase length, as long as the same fraction of it has passed.
 *
 * @param untimedReferenceWindow stands in for the phase length when the drawing
 *    phase has no timer, so an untimed round is scored like a round this long.
 *    Guesses made after it has passed earn [minPoints].
 */
data class ScoringConfiguration(
    val maxPoints: Int,
    val minPoints: Int,
    val untimedReferenceWindow: Duration,
) {
    init {
        require(minPoints in 0..maxPoints) { "minPoints must be between 0 and maxPoints" }
        require(untimedReferenceWindow.isPositive()) { "untimedReferenceWindow must be positive" }
    }
}
