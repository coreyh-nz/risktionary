package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.risk.RiskLikelihood
import nz.coreyh.risktionary.game.domain.model.risk.RiskSeverity
import kotlin.time.Instant

/** One rating submission. A player's final rating is the one with the highest [seq]. */
data class PersistedRiskRating(
    val playerId: GamePlayerId,
    val seq: Int,
    val likelihood: RiskLikelihood,
    val severity: RiskSeverity,
    val ratedAt: Instant,
)
