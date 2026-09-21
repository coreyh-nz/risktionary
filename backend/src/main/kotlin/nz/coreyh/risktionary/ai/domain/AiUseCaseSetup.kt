package nz.coreyh.risktionary.ai.domain

/**
 * Which provider, model and temperature an AI use case was configured with.
 * Recorded for each game so results can be compared across models.
 */
data class AiUseCaseSetup(
    val purpose: AiUsagePurpose,
    val provider: String,
    val model: String,
    val temperature: Double,
)
