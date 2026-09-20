package nz.coreyh.risktionary.game.application.session

import java.util.concurrent.ConcurrentHashMap
import nz.coreyh.risktionary.ai.infrastructure.dispatch.AiDispatcher
import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

class GameFeedbackSession : LockableSession() {
    private val assignments = ConcurrentHashMap<GamePlayerId, GamePlayerFeedbackAssignment>()

    /** Runs feedback generation. Outlives individual rounds, as delayed feedback is generated as one ends. */
    val feedbackDispatcher = AiDispatcher()

    /**
     * Returns the player's existing assignment, or computes and stores
     * a new one via [compute] if they don't have one yet. [compute]
     * receives the current set of all assignments so it can balance across
     * them. Synchronized so concurrent joins can't both compute against
     * the same stale counts and overshoot a combination's target share.
     */
    fun assign(
        playerId: GamePlayerId,
        compute: (Collection<GamePlayerFeedbackAssignment>) -> GamePlayerFeedbackAssignment,
    ): GamePlayerFeedbackAssignment =
        withLock {
            assignments.getOrPut(playerId) { compute(assignments.values) }
        }

    fun findAssignmentFor(playerId: GamePlayerId): GamePlayerFeedbackAssignment? = assignments[playerId]

    fun assignmentFor(playerId: GamePlayerId): GamePlayerFeedbackAssignment =
        assignments[playerId]
            ?: error("No feedback assignment found for player $playerId")
}
