package nz.coreyh.risktionary.feedback.infrastructure.ai

import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.ai.domain.AiUseCaseSetup
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions

/**
 * The chat model, provider and options one AI use case runs with. Use cases
 * that share a provider share the same [chatModel].
 */
class AiUseCaseModel(
    val provider: String,
    val chatModel: ChatModel,
    val options: ChatOptions,
) {
    val model: String get() = options.model ?: "unknown"
}

/**
 * Looks up the model configured for each AI use case, so a use case can run
 * on a different provider from the others.
 */
class AiUseCaseModels(
    private val models: Map<AiUsagePurpose, AiUseCaseModel>,
) {
    fun get(purpose: AiUsagePurpose): AiUseCaseModel = models[purpose] ?: error("No AI model is configured for $purpose")

    /** What each use case is currently configured with, in a stable order. */
    fun snapshot(): List<AiUseCaseSetup> =
        AiUsagePurpose.entries
            .mapNotNull { purpose -> models[purpose]?.let { purpose to it } }
            .map { (purpose, model) -> AiUseCaseSetup(purpose, model.provider, model.model, model.options.temperature ?: 0.0) }
}
