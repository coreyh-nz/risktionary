package nz.coreyh.risktionary.game.domain.model.round

import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import kotlin.time.Instant

/**
 * Tokens spent on one AI call during a round. [playerId] is the player the
 * call was made for, or null when it was not for one player (drawing analysis).
 */
data class GameRoundAiUsage(
    val usage: AiUsage,
    val playerId: GamePlayerId?,
    val recordedAt: Instant,
)
