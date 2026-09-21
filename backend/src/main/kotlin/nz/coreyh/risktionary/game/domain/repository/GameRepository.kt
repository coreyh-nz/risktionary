package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.user.domain.model.UserId
import kotlin.time.Instant

/**
 * Write-side record of games, kept for research. Not read during gameplay.
 */
interface GameRepository {
    /**
     * Inserts the game along with its configured phase durations and words.
     */
    fun create(
        id: GameId,
        code: String,
        hostId: UserId,
        createdAt: Instant,
        config: GameConfiguration,
    )

    fun markEnded(
        id: GameId,
        reason: GameEndReason,
        endedAt: Instant,
    )
}
