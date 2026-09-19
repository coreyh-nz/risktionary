package nz.coreyh.risktionary.game.domain.model.round.guess

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import kotlin.time.Instant

/**
 * A guess submitted by a player. [elapsedMs] is the time since the round's
 * drawing began (not since the previous guess), or null if that is unknown.
 */
data class GameRoundGuess(
    val id: GuessId,
    val playerId: GamePlayerId,
    val text: String,
    val result: GuessResultType,
    val submittedAt: Instant,
    val elapsedMs: Long?,
)
