package nz.coreyh.risktionary.game.domain.model.risk

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import kotlin.time.Clock
import kotlin.time.Instant

data class PlayerRiskRating(
    val playerId: GamePlayerId,
    val rating: RiskRating,
    val ratedAt: Instant = Clock.System.now(),
)
