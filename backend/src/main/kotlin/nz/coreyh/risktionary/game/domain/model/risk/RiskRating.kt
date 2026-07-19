package nz.coreyh.risktionary.game.domain.model.risk

data class RiskRating(
    val likelihood: RiskLikelihood,
    val severity: RiskSeverity,
)
