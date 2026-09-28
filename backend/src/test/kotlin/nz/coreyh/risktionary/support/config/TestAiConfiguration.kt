package nz.coreyh.risktionary.support.config

import io.mockk.mockk
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModel
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModels
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Replaces the real AI models in integration tests, so nothing under test can
 * make a network call to an AI provider.
 */
@TestConfiguration
class TestAiConfiguration {
    @Bean
    @Primary
    fun testAiUseCaseModels(): AiUseCaseModels {
        val chatModel = mockk<ChatModel>(relaxed = true)
        return AiUseCaseModels(
            AiUsagePurpose.entries.associateWith {
                AiUseCaseModel(
                    "test-provider",
                    chatModel,
                    ChatOptions
                        .builder()
                        .model("test-model")
                        .temperature(0.5)
                        .build(),
                )
            },
        )
    }
}
