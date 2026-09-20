package nz.coreyh.risktionary.feedback.infrastructure.prompt

import nz.coreyh.risktionary.ai.infrastructure.prompt.PromptTemplateLoader
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGuessContext
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.content.Media
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class FeedbackPromptTemplates(
    private val promptTemplateLoader: PromptTemplateLoader,
) {
    private val factGenerationSystemResources =
        mapOf(
            FeedbackTimingCondition.INSTANT to promptTemplateLoader.resource(FACT_GENERATION_SYSTEM),
            FeedbackTimingCondition.DELAYED to promptTemplateLoader.resource(FACT_GENERATION_DELAYED_SYSTEM),
        )
    private val oneAttemptRulesResource = promptTemplateLoader.resource(SHARED_ONE_ATTEMPT)
    private val noteFormRulesResource = promptTemplateLoader.resource(SHARED_NOTE_FORM)
    private val factGenerationUserResource = promptTemplateLoader.resource(FACT_GENERATION_USER)

    private val framingRewriteSystemResource = promptTemplateLoader.resource(FRAMING_REWRITE_SYSTEM)
    private val framingRewriteUserResource = promptTemplateLoader.resource(FRAMING_REWRITE_USER)

    private val framingTimingResources =
        mapOf(
            FeedbackTimingCondition.INSTANT to promptTemplateLoader.resource(FRAMING_TIMING_INSTANT),
            FeedbackTimingCondition.DELAYED to promptTemplateLoader.resource(FRAMING_TIMING_DELAYED),
        )

    private val framingConditionResources =
        mapOf(
            FeedbackFramingCondition.CORRECTIVE to promptTemplateLoader.resource(FRAMING_CONDITIONS_CORRECTIVE),
            FeedbackFramingCondition.NEUTRAL to promptTemplateLoader.resource(FRAMING_CONDITIONS_NEUTRAL),
            FeedbackFramingCondition.POSITIVE to promptTemplateLoader.resource(FRAMING_CONDITIONS_POSITIVE),
        )

    private val drawingAnalysisSystemResource = promptTemplateLoader.resource(DRAWING_ANALYSIS_SYSTEM)
    private val drawingAnalysisUserResource = promptTemplateLoader.resource(DRAWING_ANALYSIS_USER)

    /**
     * The fact generation instructions for [timing]. Both variants are built
     * from the same shared rules so the two conditions stay consistent.
     */
    fun factGenerationSystem(timing: FeedbackTimingCondition): Message =
        promptTemplateLoader.systemMessage(
            factGenerationSystemResources.getValue(timing),
            mapOf(
                "oneAttemptRules" to promptTemplateLoader.raw(oneAttemptRulesResource),
                "noteFormRules" to promptTemplateLoader.raw(noteFormRulesResource),
            ),
        )

    fun factGenerationUser(
        guesses: List<FeedbackGuessContext>,
        correctAnswer: String,
        synonyms: List<String>,
        description: String,
        timing: FeedbackTimingCondition,
    ): Message =
        promptTemplateLoader.userMessage(
            factGenerationUserResource,
            mapOf(
                "correctAnswer" to correctAnswer,
                "synonyms" to synonyms.joinToString(separator = ","),
                "description" to description,
                "guesses" to formatGuesses(guesses, includeTimeRemaining = timing == FeedbackTimingCondition.INSTANT),
            ),
        )

    fun framingRewriteSystem(
        condition: FeedbackFramingCondition,
        timing: FeedbackTimingCondition,
    ): Message =
        promptTemplateLoader.systemMessage(
            framingRewriteSystemResource,
            mapOf(
                "framingRules" to framingRulesFor(condition),
                "timingConstraints" to promptTemplateLoader.raw(framingTimingResources.getValue(timing)),
            ),
        )

    fun framingRewriteUser(fact: String): Message = promptTemplateLoader.userMessage(framingRewriteUserResource, mapOf("fact" to fact))

    fun drawingAnalysisSystem(): Message = promptTemplateLoader.systemMessage(drawingAnalysisSystemResource)

    fun drawingAnalysisMediaUser(
        word: String,
        media: Media,
    ): Message = promptTemplateLoader.userMediaMessage(drawingAnalysisUserResource, media, mapOf("word" to word))

    private fun formatGuesses(
        guesses: List<FeedbackGuessContext>,
        includeTimeRemaining: Boolean,
    ): String =
        guesses.withIndex().joinToString(separator = "\n\n") { (index, guess) ->
            buildList {
                add("guess ${index + 1}: ${guess.text}")
                add("correct: ${guess.correct}")
                if (includeTimeRemaining) {
                    add("time_remaining: ${guess.timeRemainingFraction?.let { "${(it * 100).roundToInt()}%" } ?: "unknown"}")
                }
                add("drawing_note: ${guess.drawingNote ?: "none"}")
            }.joinToString(separator = "\n")
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
        private const val FACT_GENERATION_DELAYED_SYSTEM = "$FACT_GENERATION_BASE_PATH/delayed/$SYSTEM_FILE_NAME"

        private const val SHARED_BASE_PATH = "$FEEDBACK_BASE_PATH/shared"
        private const val SHARED_ONE_ATTEMPT = "$SHARED_BASE_PATH/one-attempt.st"
        private const val SHARED_NOTE_FORM = "$SHARED_BASE_PATH/note-form.st"

        private const val FRAMING_REWRITE_BASE_PATH = "$FEEDBACK_BASE_PATH/framing-rewrite"
        private const val FRAMING_REWRITE_SYSTEM = "$FRAMING_REWRITE_BASE_PATH/$SYSTEM_FILE_NAME"
        private const val FRAMING_REWRITE_USER = "$FRAMING_REWRITE_BASE_PATH/$USER_FILE_NAME"

        private const val FRAMING_TIMING_PATH = "$FRAMING_REWRITE_BASE_PATH/timing"
        private const val FRAMING_TIMING_INSTANT = "$FRAMING_TIMING_PATH/instant.st"
        private const val FRAMING_TIMING_DELAYED = "$FRAMING_TIMING_PATH/delayed.st"

        private const val FRAMING_CONDITIONS_PATH = "$FRAMING_REWRITE_BASE_PATH/conditions"
        private const val FRAMING_CONDITIONS_CORRECTIVE = "$FRAMING_CONDITIONS_PATH/corrective.st"
        private const val FRAMING_CONDITIONS_NEUTRAL = "$FRAMING_CONDITIONS_PATH/neutral.st"
        private const val FRAMING_CONDITIONS_POSITIVE = "$FRAMING_CONDITIONS_PATH/positive.st"

        private const val DRAWING_ANALYSIS_BASE_PATH = "$BASE_PATH/drawing-analysis"
        private const val DRAWING_ANALYSIS_SYSTEM = "$DRAWING_ANALYSIS_BASE_PATH/$SYSTEM_FILE_NAME"
        private const val DRAWING_ANALYSIS_USER = "$DRAWING_ANALYSIS_BASE_PATH/$USER_FILE_NAME"
    }
}
