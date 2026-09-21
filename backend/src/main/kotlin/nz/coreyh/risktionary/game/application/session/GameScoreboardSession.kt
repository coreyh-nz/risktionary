package nz.coreyh.risktionary.game.application.session

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import kotlin.math.roundToInt

/**
 * The points earned in a game, per round.
 *
 * A guesser earns the points of their correct guess. The drawer earns the
 * average of the points of everyone who guessed correctly, so a drawing that
 * many players get quickly is rewarded.
 */
class GameScoreboardSession : LockableSession() {
    private class RoundScores(
        val drawerId: GamePlayerId,
    ) {
        val guesserPoints = mutableMapOf<GamePlayerId, Int>()

        fun drawerPoints(): Int = if (guesserPoints.isEmpty()) 0 else guesserPoints.values.average().roundToInt()
    }

    private val rounds = mutableMapOf<Int, RoundScores>()

    /**
     * Records the points of a correct guess. A player earns points for a round
     * once; later calls for the same guesser are ignored.
     */
    fun recordCorrectGuess(
        roundNumber: Int,
        drawerId: GamePlayerId,
        guesserId: GamePlayerId,
        points: Int,
    ): Unit =
        withLock {
            val round = rounds.getOrPut(roundNumber) { RoundScores(drawerId) }
            round.guesserPoints.putIfAbsent(guesserId, points)
        }

    /** Points every player earned in [roundNumber], drawer included. Players who earned none are absent. */
    fun roundPoints(roundNumber: Int): Map<GamePlayerId, Int> =
        withLock {
            val round = rounds[roundNumber] ?: return emptyMap()
            val points = round.guesserPoints.toMutableMap()
            if (round.guesserPoints.isNotEmpty()) points[round.drawerId] = round.drawerPoints()
            points
        }

    /** What the drawer of [roundNumber] earned, which is 0 if nobody guessed correctly. */
    fun drawerPoints(roundNumber: Int): Int = withLock { rounds[roundNumber]?.drawerPoints() ?: 0 }

    /** Every player total across all rounds so far. Players who earned nothing are absent. */
    fun totalPoints(): Map<GamePlayerId, Int> =
        withLock {
            val totals = mutableMapOf<GamePlayerId, Int>()
            rounds.keys.forEach { round -> roundPoints(round).forEach { (id, points) -> totals.merge(id, points, Int::plus) } }
            totals
        }
}
