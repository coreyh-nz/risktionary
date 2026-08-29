package nz.coreyh.risktionary.game.domain.model.action

import nz.coreyh.risktionary.game.domain.model.risk.RiskRating

data class GameRoundPhaseRiskRatingAction(
    val rating: RiskRating,
)
