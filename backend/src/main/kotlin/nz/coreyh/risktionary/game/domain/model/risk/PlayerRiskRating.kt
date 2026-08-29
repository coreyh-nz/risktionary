package nz.coreyh.risktionary.game.domain.model.risk

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

data class PlayerRiskRating(
    val playerId: GamePlayerId,
    val rating: RiskRating,
)
