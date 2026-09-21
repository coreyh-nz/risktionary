package nz.coreyh.risktionary.unit.feedback.infrastructure.prompt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import nz.coreyh.risktionary.ai.infrastructure.prompt.PromptTemplateLoader
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGuessContext
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
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

    private fun user(
        guesses: List<FeedbackGuessContext>,
        timing: FeedbackTimingCondition = FeedbackTimingCondition.INSTANT,
    ) = templates.factGenerationUser(guesses, "hospital", listOf("infirmary"), "A place for medical care", timing).text!!

    @Test
    fun `fact generation user prompt lists every guess in order with its context`() {
        val text =
            user(
                listOf(
                    guess("clinic", drawingNote = "a building with a cross", timeRemainingFraction = 0.8),
                    guess("surgery", timeRemainingFraction = 0.25),
                ),
            )

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
        user(listOf(guess("clinic"))) shouldContain "time_remaining: unknown"
    }

    @Test
    fun `fact generation user prompt leaves out time remaining for delayed feedback`() {
        val text = user(listOf(guess("clinic", drawingNote = "a cross", timeRemainingFraction = 0.8)), FeedbackTimingCondition.DELAYED)

        text shouldContain "guess 1: clinic"
        text shouldContain "drawing_note: a cross"
        text shouldNotContain "time_remaining"
    }

    private fun delayedUser(guessedCorrectly: Boolean) =
        templates
            .factGenerationUser(listOf(guess("cable")), "trip hazard", emptyList(), "desc", FeedbackTimingCondition.DELAYED, guessedCorrectly)
            .text!!

    @Test
    fun `delayed user prompt says the player went on to guess the answer when they did`() {
        val text = delayedUser(guessedCorrectly = true)

        text shouldContain "round_outcome: the player went on to guess the correct answer"
        text shouldNotContain "did not guess"
    }

    @Test
    fun `delayed user prompt says the player never got there when they did not`() {
        val text = delayedUser(guessedCorrectly = false)

        text shouldContain "round_outcome: the player did not guess the correct answer before the round ended"
        text shouldNotContain "went on to guess"
    }

    @Test
    fun `instant user prompt does not include a round outcome`() {
        user(listOf(guess("cable"))) shouldNotContain "round_outcome"
    }

    @Test
    fun `fact generation user prompt leaves no placeholders unfilled`() {
        FeedbackTimingCondition.entries.forEach { timing ->
            user(listOf(guess("clinic", true)), timing) shouldNotContain "{"
        }
    }

    @Test
    fun `instant fact generation prompt states the required properties`() {
        val text = templates.factGenerationSystem(FeedbackTimingCondition.INSTANT).text!!

        text shouldContain "never reveal"
        text shouldContain "TIME-AWARE SPECIFICITY"
        text shouldContain "ONE evolving attempt"
        text shouldContain "NOT addressed to the player"
        text shouldContain "ABOUT THE ANSWER"
        text shouldContain "substantive fact about the ANSWER"
    }

    @Test
    fun `delayed fact generation prompt asks for a round debrief that names the answer`() {
        val text = templates.factGenerationSystem(FeedbackTimingCondition.DELAYED).text!!

        text shouldContain "END of the round"
        text shouldContain "named plainly"
        text shouldContain "How the guesses progressed"
        text shouldContain "the guesses that progress"
        text shouldContain "3 to 5 sentences"
        text shouldContain "reading strategy"
        text shouldContain "round_outcome"
        text shouldNotContain "HARD LIMIT"
        text shouldNotContain "TIME-AWARE SPECIFICITY"
    }

    @Test
    fun `both fact generation prompts share the same core rules and leave no placeholders`() {
        FeedbackTimingCondition.entries.forEach { timing ->
            val text = templates.factGenerationSystem(timing).text!!

            text shouldContain "ONE evolving attempt"
            text shouldContain "NOT addressed to the player"
            text shouldContain "flat, neutral, third-person"
            text shouldNotContain "{oneAttemptRules}"
            text shouldNotContain "{noteFormRules}"
        }
    }

    @Test
    fun `framing rewrite prompt includes the rules for each framing condition and timing`() {
        FeedbackTimingCondition.entries.forEach { timing ->
            FeedbackFramingCondition.entries.forEach { condition ->
                val text = templates.framingRewriteSystem(condition, timing).text!!

                text shouldNotContain "{framingRules}"
                text shouldNotContain "{timingConstraints}"
                text shouldContain "Framing style for this feedback"
            }
        }
        templates.framingRewriteSystem(FeedbackFramingCondition.CORRECTIVE, FeedbackTimingCondition.INSTANT).text!! shouldContain "correction"
        templates.framingRewriteSystem(FeedbackFramingCondition.POSITIVE, FeedbackTimingCondition.INSTANT).text!! shouldContain "encouraging"
    }

    @Test
    fun `instant framing rewrite keeps the answer hidden and short`() {
        val text = templates.framingRewriteSystem(FeedbackFramingCondition.NEUTRAL, FeedbackTimingCondition.INSTANT).text!!

        text shouldContain "Do not reveal, spell out, or make obvious the correct answer"
        text shouldContain "1 to 2 sentences"
        text shouldNotContain "3 to 5 sentences"
    }

    @Test
    fun `delayed framing rewrite may name the answer and writes a short paragraph`() {
        val text = templates.framingRewriteSystem(FeedbackFramingCondition.NEUTRAL, FeedbackTimingCondition.DELAYED).text!!

        text shouldContain "may be named plainly"
        text shouldContain "3 to 5 sentences"
        text shouldNotContain "Do not reveal, spell out, or make obvious the correct answer"
    }

    @Test
    fun `framing rewrite forbids inventing a guess contrast for answer-focused notes in both timings`() {
        FeedbackTimingCondition.entries.forEach { timing ->
            val text = templates.framingRewriteSystem(FeedbackFramingCondition.NEUTRAL, timing).text!!

            text shouldContain "invent or assume what the player guessed"
            text shouldContain "\"narrowing\""
        }
    }
}
