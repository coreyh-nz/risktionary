package nz.coreyh.risktionary.feedback.config.properties

import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiChatOptionsConfiguration(
    private val aiUseCaseProperties: AiUseCaseProperties,
) {
    @Bean
    @Qualifier("feedbackImageAnalysisChatOptions")
    fun feedbackImageAnalysisChatOptions(): ChatOptions = buildOptions(aiUseCaseProperties.imageAnalysis)

    @Bean
    @Qualifier("feedbackFactGenerationChatOptions")
    fun feedbackFactGenerationChatOptions(): ChatOptions = buildOptions(aiUseCaseProperties.factGeneration)

    @Bean
    @Qualifier("feedbackFactFramingRewriteChatOptions")
    fun feedbackFactFramingRewriteChatOptions(): ChatOptions = buildOptions(aiUseCaseProperties.factFramingRewrite)

    private fun buildOptions(config: AiUseCaseProperties.UseCaseConfig): ChatOptions =
        ChatOptions
            .builder()
            .model(config.model)
            .temperature(config.temperature)
            .build()
}
