package nz.coreyh.risktionary.feedback.infrastructure.prompt

import nz.coreyh.risktionary.ai.infrastructure.prompt.PromptTemplateLoader
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGuessContext
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.content.Media
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class FeedbackPromptTemplates(
    private val promptTemplateLoader: PromptTemplateLoader,
) {
    private val factGenerationSystemResource = promptTemplateLoader.resource(FACT_GENERATION_SYSTEM)
    private val factGenerationUserResource = promptTemplateLoader.resource(FACT_GENERATION_USER)

    private val framingRewriteSystemResource = promptTemplateLoader.resource(FRAMING_REWRITE_SYSTEM)
    private val framingRewriteUserResource = promptTemplateLoader.resource(FRAMING_REWRITE_USER)

    private val framingConditionResources =
        mapOf(
            FeedbackFramingCondition.CORRECTIVE to promptTemplateLoader.resource(FRAMING_CONDITIONS_CORRECTIVE),
            FeedbackFramingCondition.NEUTRAL to promptTemplateLoader.resource(FRAMING_CONDITIONS_NEUTRAL),
            FeedbackFramingCondition.POSITIVE to promptTemplateLoader.resource(FRAMING_CONDITIONS_POSITIVE),
        )

    private val drawingAnalysisSystemResource = promptTemplateLoader.resource(DRAWING_ANALYSIS_SYSTEM)
    private val drawingAnalysisUserResource = promptTemplateLoader.resource(DRAWING_ANALYSIS_USER)

    fun factGenerationSystem(): Message = promptTemplateLoader.systemMessage(factGenerationSystemResource)

    fun factGenerationUser(
        guesses: List<FeedbackGuessContext>,
        correctAnswer: String,
        synonyms: List<String>,
        description: String,
    ): Message =
        promptTemplateLoader.userMessage(
            factGenerationUserResource,
            mapOf(
                "correctAnswer" to correctAnswer,
                "synonyms" to synonyms.joinToString(separator = ","),
                "description" to description,
                "guesses" to formatGuesses(guesses),
            ),
        )

    fun framingRewriteSystem(condition: FeedbackFramingCondition): Message =
        promptTemplateLoader.systemMessage(
            framingRewriteSystemResource,
            mapOf("framingRules" to framingRulesFor(condition)),
        )

    fun framingRewriteUser(fact: String): Message = promptTemplateLoader.userMessage(framingRewriteUserResource, mapOf("fact" to fact))

    fun drawingAnalysisSystem(): Message = promptTemplateLoader.systemMessage(drawingAnalysisSystemResource)

    fun drawingAnalysisMediaUser(
        word: String,
        media: Media,
    ): Message = promptTemplateLoader.userMediaMessage(drawingAnalysisUserResource, media, mapOf("word" to word))

    private fun formatGuesses(guesses: List<FeedbackGuessContext>): String =
        guesses.withIndex().joinToString(separator = "\n\n") { (index, guess) ->
            val timeRemaining = guess.timeRemainingFraction?.let { "${(it * 100).roundToInt()}%" } ?: "unknown"
            listOf(
                "guess ${index + 1}: ${guess.text}",
                "correct: ${guess.correct}",
                "time_remaining: $timeRemaining",
                "drawing_note: ${guess.drawingNote ?: "none"}",
            ).joinToString(separator = "\n")
        }

    private fun framingRulesFor(condition: FeedbackFramingCondition): String =
        promptTemplateLoader.raw(framingConditionResources.getValue(condition))

    companion object {
        private const val BASE_PATH = "ai/prompts"
        private const val SYSTEM_FILE_NAME = "system.st"
        private const val USER_FILE_NAME = "user.st"

        private const val FEEDBACK_BASE_PATH = "$BASE_PATH/feedback"

        private const val FACT_GENERATION_BASE_PATH = "$FEEDBACK_BASE_PATH/fact-generation"
        private const val FACT_GENERATION_SYSTEM = "$FACT_GENERATION_BASE_PATH/$SYSTEM_FILE_NAME"
        private const val FACT_GENERATION_USER = "$FACT_GENERATION_BASE_PATH/$USER_FILE_NAME"

        private const val FRAMING_REWRITE_BASE_PATH = "$FEEDBACK_BASE_PATH/framing-rewrite"
        private const val FRAMING_REWRITE_SYSTEM = "$FRAMING_REWRITE_BASE_PATH/$SYSTEM_FILE_NAME"
        private const val FRAMING_REWRITE_USER = "$FRAMING_REWRITE_BASE_PATH/$USER_FILE_NAME"

        private const val FRAMING_CONDITIONS_PATH = "$FRAMING_REWRITE_BASE_PATH/conditions"
        private const val FRAMING_CONDITIONS_CORRECTIVE = "$FRAMING_CONDITIONS_PATH/corrective.st"
        private const val FRAMING_CONDITIONS_NEUTRAL = "$FRAMING_CONDITIONS_PATH/neutral.st"
        private const val FRAMING_CONDITIONS_POSITIVE = "$FRAMING_CONDITIONS_PATH/positive.st"

        private const val DRAWING_ANALYSIS_BASE_PATH = "$BASE_PATH/drawing-analysis"
        private const val DRAWING_ANALYSIS_SYSTEM = "$DRAWING_ANALYSIS_BASE_PATH/$SYSTEM_FILE_NAME"
        private const val DRAWING_ANALYSIS_USER = "$DRAWING_ANALYSIS_BASE_PATH/$USER_FILE_NAME"
    }
}
