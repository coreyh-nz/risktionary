package nz.coreyh.risktionary.support.config

import io.mockk.mockk
import org.springframework.ai.chat.model.ChatModel
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Replaces the real chat model in integration tests, so nothing under test can
 * make a network call to an AI provider.
 */
@TestConfiguration
class TestAiConfiguration {
    @Bean
    @Primary
    fun testChatModel(): ChatModel = mockk(relaxed = true)
}
