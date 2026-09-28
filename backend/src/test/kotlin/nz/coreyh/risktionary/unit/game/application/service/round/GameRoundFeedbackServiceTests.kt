package nz.coreyh.risktionary.unit.game.application.service.round

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationResult
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.feedback.domain.service.FeedbackService
import nz.coreyh.risktionary.game.application.service.round.GameRoundFeedbackService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.TimeWindow
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.domain.model.round.chat.createChatMessageId
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.support.factory.game.createTestGameConfiguration
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSession
import nz.coreyh.risktionary.support.factory.word.createTestWord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class GameRoundFeedbackServiceTests {
    private val feedbackService = mockk<FeedbackService>()
    private val gameEventPublisher = mockk<GameEventPublisher>(relaxed = true)
    private val service = GameRoundFeedbackService(feedbackService, gameEventPublisher)

    private val games = mutableListOf<GameSession>()

    @AfterEach
    fun tearDown() {
        games.filter { it.config.feedbackGenerationEnabled }.forEach { it.feedback.feedbackDispatcher.close() }
    }

    private fun success(framing: FeedbackFramingCondition = FeedbackFramingCondition.NEUTRAL) =
        FeedbackGenerationResult(
            status = FeedbackGenerationStatus.SUCCESS,
            factText = "fact",
            framedText = "framed",
            framingCondition = framing,
            generatedAt = Clock.System.now(),
            usage =
                listOf(
                    AiUsage(AiUsagePurpose.FACT_GENERATION, "test-provider", "test-model", 10, 5, 15),
                    AiUsage(AiUsagePurpose.FRAMING_REWRITE, "test-provider", "test-model", 4, 3, 7),
                ),
        )

    private inner class Scenario(
        timing: FeedbackTimingCondition,
        framing: FeedbackFramingCondition = FeedbackFramingCondition.CORRECTIVE,
        mode: FeedbackGenerationMode = FeedbackGenerationMode.AI,
        timed: Boolean = true,
    ) {
        val session: GameSession = createTestGameSession(config = createTestGameConfiguration(feedbackGenerationMode = mode))
        val drawer: GamePlayerSession = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        val player: GamePlayerSession = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        val round = GameRoundSession(createRoundId(), session, createTestWord())

        init {
            games.add(session)
            session.requestJoin(drawer)
            session.requestJoin(player)
            if (mode != FeedbackGenerationMode.NONE) {
                session.feedback.assign(player.id) { GamePlayerFeedbackAssignment(framing, timing) }
            }
            round.selectDrawer(drawer)
            round.updatePhase(GameRoundPhase.Drawing(if (timed) TimeWindow(Clock.System.now(), 60.seconds) else null))
        }

        fun guess(
            text: String,
            result: GuessResultType = GuessResultType.INCORRECT,
        ): GameRoundGuess = round.guesses.recordGuess(player.id, text, result)
    }

    private fun publishedLatch(): CountDownLatch {
        val latch = CountDownLatch(1)
        every { gameEventPublisher.publishRoundFeedback(any(), any(), any(), any(), any()) } answers { latch.countDown() }
        return latch
    }

    private fun CountDownLatch.awaitPublished() = (await(5, TimeUnit.SECONDS)) shouldBe true

    @Test
    fun `instant feedback is generated after a guess and sent as a reply to the guess message`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT)
        val payload = slot<FeedbackFactPayload>()
        every { feedbackService.generate(any(), capture(payload)) } returns success()
        val latch = publishedLatch()
        val messageId = createChatMessageId()
        val guess = scenario.guess("clinic")

        service.onGuess(scenario.round, scenario.player, guess, messageId)
        latch.awaitPublished()

        payload.captured.guesses.map { it.text } shouldBe listOf("clinic")
        payload.captured.condition shouldBe FeedbackFramingCondition.CORRECTIVE
        val feedback =
            scenario.round.feedback
                .getAll()
                .single()
        feedback.sourceGuessIds shouldBe listOf(guess.id)
        feedback.timingCondition shouldBe FeedbackTimingCondition.INSTANT
        verify {
            gameEventPublisher.publishRoundFeedback(
                playerId = scenario.player.id,
                feedbackId = feedback.id,
                messageId = messageId,
                roundNumber = scenario.session.roundNumber,
                text = "framed",
            )
        }
    }

    @Test
    fun `instant feedback for a later guess includes the earlier guesses so far`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT)
        val payloads = mutableListOf<FeedbackFactPayload>()
        every { feedbackService.generate(any(), capture(payloads)) } returns success()
        val latch = CountDownLatch(2)
        every { gameEventPublisher.publishRoundFeedback(any(), any(), any(), any(), any()) } answers { latch.countDown() }

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())
        service.onGuess(scenario.round, scenario.player, scenario.guess("surgery"), createChatMessageId())
        latch.await(5, TimeUnit.SECONDS) shouldBe true

        payloads.map { p -> p.guesses.map { it.text } }.sortedBy { it.size } shouldBe
            listOf(listOf("clinic"), listOf("clinic", "surgery"))
    }

    @Test
    fun `delayed feedback is not generated per guess`() {
        val scenario = Scenario(FeedbackTimingCondition.DELAYED)

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())
        service.onGuess(scenario.round, scenario.player, scenario.guess("surgery"), createChatMessageId())

        verify(exactly = 0) { feedbackService.generate(any(), any()) }
        scenario.round.feedback
            .getGuessesByPlayer()
            .getValue(scenario.player.id) shouldHaveSize 2
    }

    @Test
    fun `delayed feedback is generated once from every guess when drawing ends`() {
        val scenario = Scenario(FeedbackTimingCondition.DELAYED)
        val payloads = mutableListOf<FeedbackFactPayload>()
        every { feedbackService.generate(any(), capture(payloads)) } returns success()
        val latch = publishedLatch()
        val first = scenario.guess("clinic")
        val second = scenario.guess("surgery")
        service.onGuess(scenario.round, scenario.player, first, createChatMessageId())
        service.onGuess(scenario.round, scenario.player, second, createChatMessageId())

        service.generateDelayed(scenario.round)
        latch.awaitPublished()

        payloads shouldHaveSize 1
        payloads.single().guesses.map { it.text } shouldBe listOf("clinic", "surgery")
        val feedback =
            scenario.round.feedback
                .getAll()
                .single()
        feedback.sourceGuessIds shouldBe listOf(first.id, second.id)
        feedback.timingCondition shouldBe FeedbackTimingCondition.DELAYED
        verify {
            gameEventPublisher.publishRoundFeedback(
                playerId = scenario.player.id,
                feedbackId = feedback.id,
                messageId = null,
                roundNumber = scenario.session.roundNumber,
                text = "framed",
            )
        }
    }

    @Test
    fun `delayed feedback tells the generator the player went on to guess correctly`() {
        val scenario = Scenario(FeedbackTimingCondition.DELAYED)
        val payload = slot<FeedbackFactPayload>()
        every { feedbackService.generate(any(), capture(payload)) } returns success()
        val latch = publishedLatch()
        service.onGuess(scenario.round, scenario.player, scenario.guess("cable"), createChatMessageId())
        scenario.guess("trip hazard", GuessResultType.CORRECT)

        service.generateDelayed(scenario.round)
        latch.awaitPublished()

        payload.captured.guessedCorrectly shouldBe true
        payload.captured.guesses.map { it.text } shouldBe listOf("cable")
    }

    @Test
    fun `delayed feedback tells the generator the player never guessed correctly`() {
        val scenario = Scenario(FeedbackTimingCondition.DELAYED)
        val payload = slot<FeedbackFactPayload>()
        every { feedbackService.generate(any(), capture(payload)) } returns success()
        val latch = publishedLatch()
        service.onGuess(scenario.round, scenario.player, scenario.guess("cable"), createChatMessageId())

        service.generateDelayed(scenario.round)
        latch.awaitPublished()

        payload.captured.guessedCorrectly shouldBe false
    }

    @Test
    fun `instant feedback is never generated as if the player had guessed correctly`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT)
        val payload = slot<FeedbackFactPayload>()
        every { feedbackService.generate(any(), capture(payload)) } returns success()
        val latch = publishedLatch()

        service.onGuess(scenario.round, scenario.player, scenario.guess("cable"), createChatMessageId())
        latch.awaitPublished()

        payload.captured.guessedCorrectly shouldBe false
    }

    @Test
    fun `generating delayed feedback skips instant players and players who did not guess`() {
        val instant = Scenario(FeedbackTimingCondition.INSTANT)
        every { feedbackService.generate(any(), any()) } returns success()
        val latch = publishedLatch()
        service.onGuess(instant.round, instant.player, instant.guess("clinic"), createChatMessageId())
        latch.awaitPublished()

        service.generateDelayed(instant.round)

        verify(exactly = 1) { feedbackService.generate(any(), any()) }
        Scenario(FeedbackTimingCondition.DELAYED).let { service.generateDelayed(it.round) }
        verify(exactly = 1) { feedbackService.generate(any(), any()) }
    }

    @Test
    fun `guess context carries the drawing note and time remaining current at the time of the guess`() {
        val scenario = Scenario(FeedbackTimingCondition.DELAYED)
        service.onGuess(scenario.round, scenario.player, scenario.guess("early"), createChatMessageId())
        scenario.round.drawing.record(byteArrayOf(1), "image/png", DrawingAnalysisResult.Fact("a building"), Clock.System.now())
        service.onGuess(scenario.round, scenario.player, scenario.guess("late"), createChatMessageId())

        val (early, late) =
            scenario.round.feedback
                .getGuessesByPlayer()
                .getValue(scenario.player.id)

        early.drawingNote.shouldBeNull()
        late.drawingNote shouldBe "a building"
        early.timeRemainingFraction.shouldNotBeNull()
        (early.timeRemainingFraction!! in 0.9..1.0) shouldBe true
    }

    @Test
    fun `a no-fact drawing analysis gives no drawing note`() {
        val scenario = Scenario(FeedbackTimingCondition.DELAYED)
        scenario.round.drawing.record(byteArrayOf(1), "image/png", DrawingAnalysisResult.NoFact, Clock.System.now())

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())

        scenario.round.feedback
            .getGuessesByPlayer()
            .getValue(scenario.player.id)
            .single()
            .drawingNote
            .shouldBeNull()
    }

    @Test
    fun `time remaining is unknown when the drawing phase has no timer`() {
        val scenario = Scenario(FeedbackTimingCondition.DELAYED, timed = false)

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())

        scenario.round.feedback
            .getGuessesByPlayer()
            .getValue(scenario.player.id)
            .single()
            .timeRemainingFraction
            .shouldBeNull()
    }

    @Test
    fun `a failed generation is recorded but not sent to the player`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT)
        every { feedbackService.generate(any(), any()) } returns
            success().copy(status = FeedbackGenerationStatus.FACT_FAILED, factText = null, framedText = null)

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())
        val deadline = System.currentTimeMillis() + 5_000
        while (scenario.round.feedback
                .getAll()
                .isEmpty() && System.currentTimeMillis() < deadline
        ) {
            Thread.sleep(10)
        }

        scenario.round.feedback
            .getAll()
            .single()
            .status shouldBe FeedbackGenerationStatus.FACT_FAILED
        verify(exactly = 0) { gameEventPublisher.publishRoundFeedback(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `nothing is recorded or generated when feedback generation is disabled`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT, mode = FeedbackGenerationMode.NONE)

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())
        service.generateDelayed(scenario.round)

        verify(exactly = 0) { feedbackService.generate(any(), any()) }
        scenario.round.feedback.getGuessesByPlayer() shouldBe emptyMap()
    }

    @Test
    fun `token usage of a generation is recorded on the round against the player`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT)
        every { feedbackService.generate(any(), any()) } returns success()
        val latch = publishedLatch()

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())
        latch.awaitPublished()

        val usage = scenario.round.aiUsage.getAll()
        usage.map { it.usage.purpose } shouldBe listOf(AiUsagePurpose.FACT_GENERATION, AiUsagePurpose.FRAMING_REWRITE)
        usage.map { it.playerId }.distinct() shouldBe listOf(scenario.player.id)
    }

    @Test
    fun `the round waits for a generation that is still running`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT)
        every { feedbackService.generate(any(), any()) } answers
            {
                Thread.sleep(300)
                success()
            }

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())
        scenario.round.feedback.awaitPending(5.seconds) shouldBe 0

        scenario.round.feedback.getAll() shouldHaveSize 1
    }

    @Test
    fun `a generation that finishes after the round stopped waiting is discarded`() {
        val scenario = Scenario(FeedbackTimingCondition.INSTANT)
        every { feedbackService.generate(any(), any()) } answers
            {
                Thread.sleep(400)
                success()
            }

        service.onGuess(scenario.round, scenario.player, scenario.guess("clinic"), createChatMessageId())
        scenario.round.feedback.awaitPending(50.milliseconds) shouldBe 1
        Thread.sleep(800)

        scenario.round.feedback.getAll() shouldBe emptyList()
        scenario.round.aiUsage.getAll() shouldBe emptyList()
        verify(exactly = 0) { gameEventPublisher.publishRoundFeedback(any(), any(), any(), any(), any()) }
    }
}
