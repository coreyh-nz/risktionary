package nz.coreyh.risktionary.unit.feedback.infrastructure.generator

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.mockk
import nz.coreyh.risktionary.ai.domain.AiResponse
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.ai.infrastructure.prompt.PromptTemplateLoader
import nz.coreyh.risktionary.ai.infrastructure.service.AiChatService
import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGuessContext
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModel
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModels
import nz.coreyh.risktionary.feedback.infrastructure.generator.AiFeedbackGenerator
import nz.coreyh.risktionary.feedback.infrastructure.prompt.FeedbackPromptTemplates
import nz.coreyh.risktionary.game.domain.model.round.guess.createGuessId
import nz.coreyh.risktionary.support.factory.word.createTestWord
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.core.io.DefaultResourceLoader

class AiFeedbackGeneratorTests {
    private val aiChatService = mockk<AiChatService>()
    private val factChatModel = mockk<ChatModel>()
    private val rewriteChatModel = mockk<ChatModel>()
    private val generator =
        AiFeedbackGenerator(
            aiUseCaseModels =
                AiUseCaseModels(
                    mapOf(
                        AiUsagePurpose.FACT_GENERATION to
                            AiUseCaseModel("fact-provider", factChatModel, ChatOptions.builder().model("fact-model").build()),
                        AiUsagePurpose.FRAMING_REWRITE to
                            AiUseCaseModel("rewrite-provider", rewriteChatModel, ChatOptions.builder().model("rewrite-model").build()),
                    ),
                ),
            feedbackPromptTemplates = FeedbackPromptTemplates(PromptTemplateLoader(DefaultResourceLoader())),
            aiChatService = aiChatService,
        )

    private fun payload(timing: FeedbackTimingCondition) =
        FeedbackFactPayload(
            guesses = listOf(FeedbackGuessContext(createGuessId(), "cable", false, "a cable on a floor", 0.5)),
            word = createTestWord(),
            condition = FeedbackFramingCondition.NEUTRAL,
            timing = timing,
        )

    /** Runs the generator and returns the system prompt of each AI call it made, in order. */
    private fun systemPromptsFor(timing: FeedbackTimingCondition): List<String> {
        val systemPrompts = mutableListOf<String>()
        every { aiChatService.send<String>(any(), any(), any(), String::class) } answers
            {
                systemPrompts += secondArg<List<Message>>().first().text!!
                AiResponse.Success("generated text", 1, 1, 2)
            }

        val result = generator.generate(payload(timing))

        result.status shouldBe FeedbackGenerationStatus.SUCCESS
        return systemPrompts
    }

    @Test
    fun `instant feedback uses the instant fact and rewrite prompts`() {
        val (fact, rewrite) = systemPromptsFor(FeedbackTimingCondition.INSTANT)

        fact shouldContain "HARD LIMIT"
        fact shouldNotContain "END of the round"
        rewrite shouldContain "1 to 2 sentences"
    }

    @Test
    fun `delayed feedback uses the delayed fact and rewrite prompts`() {
        val (fact, rewrite) = systemPromptsFor(FeedbackTimingCondition.DELAYED)

        fact shouldContain "END of the round"
        fact shouldNotContain "HARD LIMIT"
        rewrite shouldContain "3 to 5 sentences"
    }

    @Test
    fun `token usage of both calls is reported`() {
        every { aiChatService.send<String>(any(), any(), any(), String::class) } returns AiResponse.Success("text", 10, 5, 15)

        val result = generator.generate(payload(FeedbackTimingCondition.DELAYED))

        result.usage.map { it.model } shouldBe listOf("fact-model", "rewrite-model")
        result.usage.map { it.provider } shouldBe listOf("fact-provider", "rewrite-provider")
        result.usage.sumOf { it.totalTokens } shouldBe 30
    }

    @Test
    fun `each step runs on the chat model of its own use case`() {
        val usedModels = mutableListOf<ChatModel>()
        every { aiChatService.send(any(), any(), any(), String::class) } answers
            {
                usedModels += firstArg<ChatModel>()
                AiResponse.Success("text", 1, 1, 2)
            }

        generator.generate(payload(FeedbackTimingCondition.INSTANT))

        usedModels shouldBe listOf(factChatModel, rewriteChatModel)
    }
}
