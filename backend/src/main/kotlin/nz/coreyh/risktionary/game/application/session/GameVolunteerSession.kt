package nz.coreyh.risktionary.game.application.session

import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Manages the volunteer pool for an active game session.
 *
 * Tracks which players have volunteered to draw, when they volunteered,
 * and how many times they have drawn across all rounds.
 *
 * Volunteers are ordered by:
 * 1. Draw count ascending — players who have drawn fewer times appear first.
 * 2. Volunteer time ascending — earlier volunteers appear first within the same draw count.
 */
class GameVolunteerSession(
    private val clock: Clock = Clock.System,
) : LockableSession() {
    private data class VolunteerEntry(
        val playerId: GamePlayerId,
        val volunteeredAt: Instant,
    )

    private val volunteers: MutableList<VolunteerEntry> = mutableListOf()
    private val drawCounts: MutableMap<GamePlayerId, Int> = mutableMapOf()

    /**
     * Returns the current volunteer list ordered by draw count then volunteer time.
     *
     * Players with fewer draws appear first. Within the same draw count,
     * earlier volunteers appear first.
     */
    fun getVolunteers(): List<GamePlayerId> =
        withLock {
            volunteers
                .sortedWith(
                    compareBy(
                        { drawCounts.getOrDefault(it.playerId, 0) },
                        { it.volunteeredAt },
                    ),
                ).map { it.playerId }
        }

    /**
     * Returns whether the given player is currently in the volunteer pool.
     */
    fun isVolunteering(playerId: GamePlayerId): Boolean = withLock { volunteers.any { it.playerId == playerId } }

    /**
     * Adds a player to the volunteer pool, recording the time they volunteered.
     *
     * @param playerId the player volunteering to draw.
     * @throws GamePlayerStateInvalidException if the player has already volunteered.
     */
    fun volunteer(playerId: GamePlayerId): Unit =
        withLock {
            if (isVolunteering(playerId)) throw GamePlayerStateInvalidException()
            volunteers.add(VolunteerEntry(playerId = playerId, volunteeredAt = clock.now()))
        }

    /**
     * Removes a player from the volunteer pool.
     *
     * @param playerId the player withdrawing their volunteer.
     * @throws GamePlayerStateInvalidException if the player has not volunteered.
     */
    fun unvolunteer(playerId: GamePlayerId): Unit =
        withLock {
            if (!volunteers.removeIf { it.playerId == playerId }) {
                throw GamePlayerStateInvalidException()
            }
        }

    /**
     * Selects a player as the drawer for the current round.
     *
     * Removes them from the volunteer pool and increments their draw count.
     *
     * @param playerId the player selected to draw.
     * @throws GamePlayerStateInvalidException if the player has not volunteered.
     */
    fun selectDrawer(playerId: GamePlayerId): Unit =
        withLock {
            if (!volunteers.removeIf { it.playerId == playerId }) {
                throw GamePlayerStateInvalidException()
            }
            drawCounts[playerId] = drawCounts.getOrDefault(playerId, 0) + 1
        }

    /**
     * Removes a player from the volunteer pool if present.
     * No-ops if the player has not volunteered. Used for clean-up on disconnect.
     */
    fun removeIfPresent(playerId: GamePlayerId): Unit = withLock { volunteers.removeIf { it.playerId == playerId } }
}
