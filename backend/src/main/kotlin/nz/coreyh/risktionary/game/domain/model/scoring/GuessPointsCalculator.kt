package nz.coreyh.risktionary.game.domain.model.scoring

import kotlin.math.roundToInt
import kotlin.time.Duration

/**
 * Works out how many points a correct guess is worth from how far into the
 * drawing phase it was made.
 */
object GuessPointsCalculator {
    /**
     * @param elapsed time since the drawing phase began, or null if unknown (treated as the very start).
     * @param drawingDuration the length of the drawing phase, or null if it has no timer.
     */
    fun pointsFor(
        elapsed: Duration?,
        drawingDuration: Duration?,
        config: ScoringConfiguration,
    ): Int {
        val window = drawingDuration?.takeIf { it.isPositive() } ?: config.untimedReferenceWindow
        val progress = ((elapsed ?: Duration.ZERO) / window).coerceIn(0.0, 1.0)
        return (config.minPoints + (config.maxPoints - config.minPoints) * (1.0 - progress)).roundToInt()
    }
}
