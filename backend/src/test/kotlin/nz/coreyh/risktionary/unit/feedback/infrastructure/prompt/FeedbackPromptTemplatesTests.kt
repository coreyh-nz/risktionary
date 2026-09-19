package nz.coreyh.risktionary.unit.feedback.infrastructure.prompt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import nz.coreyh.risktionary.ai.infrastructure.prompt.PromptTemplateLoader
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGuessContext
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.infrastructure.prompt.FeedbackPromptTemplates
import nz.coreyh.risktionary.game.domain.model.round.guess.createGuessId
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader

class FeedbackPromptTemplatesTests {
    private val templates = FeedbackPromptTemplates(PromptTemplateLoader(DefaultResourceLoader()))

    private fun guess(
        text: String,
        correct: Boolean = false,
        drawingNote: String? = null,
        timeRemainingFraction: Double? = null,
    ) = FeedbackGuessContext(createGuessId(), text, correct, drawingNote, timeRemainingFraction)

    @Test
    fun `fact generation user prompt lists every guess in order with its context`() {
        val message =
            templates.factGenerationUser(
                guesses =
                    listOf(
                        guess("clinic", drawingNote = "a building with a cross", timeRemainingFraction = 0.8),
                        guess("surgery", timeRemainingFraction = 0.25),
                    ),
                correctAnswer = "hospital",
                synonyms = listOf("infirmary"),
                description = "A place for medical care",
            )

        val text = message.text!!
        text shouldContain "correct_answer: hospital"
        text shouldContain "synonyms: infirmary"
        text shouldContain "description: A place for medical care"
        text shouldContain "guess 1: clinic"
        text shouldContain "time_remaining: 80%"
        text shouldContain "drawing_note: a building with a cross"
        text shouldContain "guess 2: surgery"
        text shouldContain "time_remaining: 25%"
        text shouldContain "drawing_note: none"
        (text.indexOf("guess 1") < text.indexOf("guess 2")) shouldBe true
    }

    @Test
    fun `fact generation user prompt marks time remaining unknown when the phase has no timer`() {
        val message = templates.factGenerationUser(listOf(guess("clinic")), "hospital", emptyList(), "desc")

        message.text!! shouldContain "time_remaining: unknown"
    }

    @Test
    fun `fact generation user prompt leaves no placeholders unfilled`() {
        val message = templates.factGenerationUser(listOf(guess("clinic", true)), "hospital", listOf("a", "b"), "desc")

        message.text!! shouldNotContain "{"
    }

    @Test
    fun `fact generation system prompt states the required properties`() {
        val text = templates.factGenerationSystem().text!!

        text shouldContain "never reveal"
        text shouldContain "TIME-AWARE SPECIFICITY"
        text shouldContain "ONE evolving attempt"
        text shouldContain "NOT addressed to the player"
        text shouldContain "ABOUT THE ANSWER"
        text shouldContain "substantive fact about the ANSWER"
    }

    @Test
    fun `framing rewrite system prompt includes the rules for each framing condition`() {
        FeedbackFramingCondition.entries.forEach { condition ->
            val text = templates.framingRewriteSystem(condition).text!!

            text shouldNotContain "{framingRules}"
            text shouldContain "Framing style for this feedback"
        }
        templates.framingRewriteSystem(FeedbackFramingCondition.CORRECTIVE).text!! shouldContain "correction"
        templates.framingRewriteSystem(FeedbackFramingCondition.POSITIVE).text!! shouldContain "encouraging"
    }

    @Test
    fun `framing rewrite system prompt forbids inventing a guess contrast for answer-focused notes`() {
        val text = templates.framingRewriteSystem(FeedbackFramingCondition.NEUTRAL).text!!

        text shouldContain "invent or assume what the player guessed"
        text shouldContain "\"narrowing\""
    }
}
