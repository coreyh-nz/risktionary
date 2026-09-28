package nz.coreyh.risktionary.feedback.infrastructure.generator

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.ai.domain.AiResponse
import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.ai.domain.toUsage
import nz.coreyh.risktionary.ai.infrastructure.service.AiChatService
import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationResult
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.feedback.domain.service.FeedbackGenerator
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModels
import nz.coreyh.risktionary.feedback.infrastructure.prompt.FeedbackPromptTemplates
import org.springframework.stereotype.Component
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

/**
 * Generates feedback in two AI steps: a neutral fact note from the guesses
 * made so far, then a rewrite of that fixed note in the requested framing.
 */
@Component
class AiFeedbackGenerator(
    private val aiUseCaseModels: AiUseCaseModels,
    private val feedbackPromptTemplates: FeedbackPromptTemplates,
    private val aiChatService: AiChatService,
    private val clock: Clock = Clock.System,
) : FeedbackGenerator {
    private val factModel get() = aiUseCaseModels.get(AiUsagePurpose.FACT_GENERATION)
    private val rewriteModel get() = aiUseCaseModels.get(AiUsagePurpose.FRAMING_REWRITE)

    override fun generate(payload: FeedbackFactPayload): FeedbackGenerationResult {
        val framingCondition = payload.condition
        val usage = mutableListOf<AiUsage>()

        val factResponse = generateFact(payload)
        if (factResponse !is AiResponse.Success) {
            logger.warn { "Could not generate a feedback fact." }
            return result(FeedbackGenerationStatus.FACT_FAILED, null, null, framingCondition, usage)
        }
        usage += factResponse.toUsage(AiUsagePurpose.FACT_GENERATION, factModel.provider, factModel.model)

        val fact = factResponse.data
        logger.debug { "Generated feedback fact for ${payload.guesses.size} guess(es): \"$fact\". Cost: ${factResponse.costToString()}" }

        val framingRewriteResponse = generateFramingRewrite(fact, framingCondition, payload.timing)
        if (framingRewriteResponse !is AiResponse.Success) {
            logger.warn { "Could not generate a feedback fact frame rewrite." }
            return result(FeedbackGenerationStatus.FRAMING_FAILED, fact, null, framingCondition, usage)
        }
        usage += framingRewriteResponse.toUsage(AiUsagePurpose.FRAMING_REWRITE, rewriteModel.provider, rewriteModel.model)

        val framingRewrite = framingRewriteResponse.data
        logger.debug {
            "Generated feedback rewrite with condition ${framingCondition.name}: \"$framingRewrite\". Cost: ${framingRewriteResponse.costToString()}"
        }

        return result(FeedbackGenerationStatus.SUCCESS, fact, framingRewrite, framingCondition, usage)
    }

    private fun result(
        status: FeedbackGenerationStatus,
        fact: String?,
        framed: String?,
        framingCondition: FeedbackFramingCondition,
        usage: List<AiUsage>,
    ) = FeedbackGenerationResult(
        status = status,
        factText = fact,
        framedText = framed,
        framingCondition = framingCondition,
        generatedAt = clock.now(),
        usage = usage.toList(),
    )

    private fun generateFact(payload: FeedbackFactPayload): AiResponse<String> {
        val word = payload.word
        return aiChatService.send(
            chatModel = factModel.chatModel,
            messages =
                listOf(
                    feedbackPromptTemplates.factGenerationSystem(payload.timing),
                    feedbackPromptTemplates.factGenerationUser(
                        guesses = payload.guesses,
                        correctAnswer = word.value,
                        synonyms = word.synonyms,
                        description = word.descriptionText,
                        timing = payload.timing,
                        guessedCorrectly = payload.guessedCorrectly,
                    ),
                ),
            options = factModel.options,
            responseClass = String::class,
        )
    }

    private fun generateFramingRewrite(
        fact: String,
        condition: FeedbackFramingCondition,
        timing: FeedbackTimingCondition,
    ): AiResponse<String> =
        aiChatService.send(
            chatModel = rewriteModel.chatModel,
            messages =
                listOf(
                    feedbackPromptTemplates.framingRewriteSystem(condition, timing),
                    feedbackPromptTemplates.framingRewriteUser(fact),
                ),
            options = rewriteModel.options,
            responseClass = String::class,
        )
}
