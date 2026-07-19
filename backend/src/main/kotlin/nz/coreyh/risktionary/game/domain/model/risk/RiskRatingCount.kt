package nz.coreyh.risktionary.game.domain.model.risk

data class RiskRatingCount(
    val likelihood: RiskLikelihood,
    val severity: RiskSeverity,
    val count: Int,
)
