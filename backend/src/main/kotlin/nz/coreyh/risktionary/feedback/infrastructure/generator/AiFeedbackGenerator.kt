package nz.coreyh.risktionary.feedback.infrastructure.generator

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.ai.domain.AiResponse
import nz.coreyh.risktionary.ai.infrastructure.service.AiChatService
import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.service.FeedbackGenerator
import nz.coreyh.risktionary.feedback.infrastructure.prompt.FeedbackPromptTemplates
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

// chat model is provided by Spring AI's `OpenAiChatAutoConfiguration`,
// which is only active when the `ai` profile is on
@Component
@Qualifier("ai-feedback-generator")
@Profile("ai")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class AiFeedbackGenerator(
    private val chatModel: ChatModel,
    private val feedbackPromptTemplates: FeedbackPromptTemplates,
    private val aiChatService: AiChatService,
) : FeedbackGenerator {
    override fun generate(payload: FeedbackFactPayload) {
        val factResponse = generateFact(payload)
        if (factResponse !is AiResponse.Success) {
            logger.warn { "Could not generate a feedback fact." }
            return
        }

        val fact = factResponse.data
        logger.debug { "Generated feedback fact for \"${payload.guess}\": \"$fact\". Cost: ${factResponse.costToString()}" }

        val framingCondition = FeedbackFramingCondition.CORRECTIVE
        val framingRewriteResponse = generateFramingRewrite(fact, FeedbackFramingCondition.POSITIVE)
        if (framingRewriteResponse !is AiResponse.Success) {
            logger.warn { "Could not generate a feedback fact frame rewrite." }
            return
        }

        val framingRewrite = framingRewriteResponse.data
        logger.debug {
            "Generated feedback fact rewrite for \"${payload.guess}\" with condition ${framingCondition.name}: \"$framingRewrite\". Cost: ${framingRewriteResponse.costToString()}"
        }
    }

    private fun generateFact(payload: FeedbackFactPayload): AiResponse<String> {
        val word = payload.word
        val options = OpenAiChatOptions.builder().model("gemini-3.5-flash-lite").build()
        return aiChatService.send<String>(
            chatModel = chatModel,
            messages =
                listOf(
                    feedbackPromptTemplates.factGenerationSystem(),
                    feedbackPromptTemplates.factGenerationUser(
                        submittedGuess = payload.guess,
                        correctAnswer = word.value,
                        correct = payload.correct,
                        synonyms = word.synonyms,
                        description = "", // TODO
                    ),
                ),
            options = options,
        )
    }

    private fun generateFramingRewrite(
        fact: String,
        condition: FeedbackFramingCondition,
    ): AiResponse<String> {
        val options = OpenAiChatOptions.builder().model("gemini-3.5-flash-lite").build()
        return aiChatService.send<String>(
            chatModel = chatModel,
            messages =
                listOf(
                    feedbackPromptTemplates.framingRewriteSystem(condition),
                    feedbackPromptTemplates.framingRewriteUser(fact),
                ),
            options = options,
        )
    }
}
