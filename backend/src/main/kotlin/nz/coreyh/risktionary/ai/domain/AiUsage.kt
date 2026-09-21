package nz.coreyh.risktionary.ai.domain

/**
 * What an AI call was made for.
 */
enum class AiUsagePurpose {
    DRAWING_ANALYSIS,
    FACT_GENERATION,
    FRAMING_REWRITE,
}

/**
 * Token usage reported by the provider for one successful AI call.
 */
data class AiUsage(
    val purpose: AiUsagePurpose,
    val provider: String,
    val model: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
)

fun AiResponse.Success<*>.toUsage(
    purpose: AiUsagePurpose,
    provider: String,
    model: String?,
): AiUsage =
    AiUsage(
        purpose = purpose,
        provider = provider,
        model = model ?: "unknown",
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        totalTokens = totalTokens,
    )
