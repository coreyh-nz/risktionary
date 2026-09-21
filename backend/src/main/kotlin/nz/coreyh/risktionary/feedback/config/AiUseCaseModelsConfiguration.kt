package nz.coreyh.risktionary.feedback.config

import nz.coreyh.risktionary.ai.config.AiProviderProperties
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.feedback.config.properties.AiUseCaseProperties
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModel
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModels
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Builds the chat model each AI use case runs on from `app.ai.providers` and
 * `app.feedback.ai.use-cases`.
 *
 * A model is only built for providers that a use case actually uses, so
 * providers that are configured but unused need no credentials. A use case that
 * names an unknown provider, or one without an API key, fails the application
 * at startup rather than at the first request.
 */
@Configuration
class AiUseCaseModelsConfiguration {
    @Bean
    fun aiUseCaseModels(
        providerProperties: AiProviderProperties,
        useCaseProperties: AiUseCaseProperties,
    ): AiUseCaseModels {
        val chatModels = mutableMapOf<String, ChatModel>()

        val useCases =
            mapOf(
                AiUsagePurpose.DRAWING_ANALYSIS to ("image-analysis" to useCaseProperties.imageAnalysis),
                AiUsagePurpose.FACT_GENERATION to ("fact-generation" to useCaseProperties.factGeneration),
                AiUsagePurpose.FRAMING_REWRITE to ("fact-framing-rewrite" to useCaseProperties.factFramingRewrite),
            )

        return AiUseCaseModels(
            useCases.mapValues { (_, useCase) ->
                val (name, config) = useCase
                val provider =
                    providerProperties.providers[config.provider]
                        ?: throw IllegalStateException(
                            "AI use case '$name' uses provider '${config.provider}', which is not configured under " +
                                "app.ai.providers (configured: ${providerProperties.providers.keys.sorted()})",
                        )
                check(provider.apiKey.isNotBlank()) {
                    "AI provider '${config.provider}' is used by AI use case '$name' but has no api-key. " +
                        "Providers that need no key (for example Ollama) still need a placeholder value."
                }

                AiUseCaseModel(
                    provider = config.provider,
                    chatModel = chatModels.getOrPut(config.provider) { buildChatModel(provider) },
                    options = ChatOptions.builder().model(config.model).temperature(config.temperature).build(),
                )
            },
        )
    }

    fun buildChatModel(provider: AiProviderProperties.Provider): ChatModel =
        OpenAiChatModel
            .builder()
            .options(
                OpenAiChatOptions
                    .builder()
                    .baseUrl(provider.baseUrl)
                    .apiKey(provider.apiKey)
                    .timeout(provider.timeout)
                    .maxRetries(provider.maxRetries)
                    .customHeaders(provider.headers)
                    .build(),
            ).build()
}
