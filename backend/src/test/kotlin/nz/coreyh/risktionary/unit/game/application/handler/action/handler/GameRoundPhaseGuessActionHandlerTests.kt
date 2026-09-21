package nz.coreyh.risktionary.unit.game.application.handler.action.handler

import io.kotest.matchers.comparables.shouldBeBetween
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nz.coreyh.risktionary.game.application.handler.action.handler.GameRoundPhaseGuessActionHandler
import nz.coreyh.risktionary.game.application.service.round.GameRoundFeedbackService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionChatService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.TimeWindow
import nz.coreyh.risktionary.game.domain.model.action.GameRoundPhaseGuessAction
import nz.coreyh.risktionary.game.domain.model.action.GameRoundPhaseGuessActionResult
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSession
import nz.coreyh.risktionary.support.factory.word.createTestWord
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class GameRoundPhaseGuessActionHandlerTests {
    private val eventPublisher = mockk<GameEventPublisher>(relaxed = true)
    private val handler =
        GameRoundPhaseGuessActionHandler(
            GameRoundSessionChatService(eventPublisher),
            eventPublisher,
            mockk<GameRoundFeedbackService>(relaxed = true),
        )

    private val game: GameSession = createTestGameSession()
    private val drawer: GamePlayerSession = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
    private val guesser: GamePlayerSession = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
    private val round = GameRoundSession(createRoundId(), game, createTestWord(value = "injury"))

    private fun startDrawing(timed: Boolean): GameRoundPhase.Drawing {
        game.requestJoin(drawer)
        game.requestJoin(guesser)
        round.selectDrawer(drawer)
        val phase = GameRoundPhase.Drawing(if (timed) TimeWindow(Clock.System.now(), 60.seconds) else null)
        round.updatePhase(phase)
        return phase
    }

    private fun guess(
        phase: GameRoundPhase.Drawing,
        text: String,
    ) = handler.handle(round, phase, guesser, GameRoundPhaseGuessAction(text))

    @Test
    fun `a correct guess early in a timed round earns close to the maximum and is scored for the guesser and drawer`() {
        val phase = startDrawing(timed = true)

        val result = guess(phase, "injury")

        (result is GameRoundPhaseGuessActionResult.Consumed) shouldBe true
        val points =
            round.guesses
                .getGuesses()
                .single()
                .points!!
        points.shouldBeBetween(950, 1000)
        game.scoreboard.roundPoints(game.roundNumber)[guesser.id] shouldBe points
        game.scoreboard.roundPoints(game.roundNumber)[drawer.id] shouldBe points
    }

    @Test
    fun `a correct guess in an untimed round is scored against the reference window`() {
        val phase = startDrawing(timed = false)

        guess(phase, "injury")

        round.guesses
            .getGuesses()
            .single()
            .points!!
            .shouldBeBetween(950, 1000)
    }

    @Test
    fun `an incorrect guess earns nothing`() {
        val phase = startDrawing(timed = true)

        val result = guess(phase, "nonsense")

        (result is GameRoundPhaseGuessActionResult.Skipped) shouldBe true
        round.guesses
            .getGuesses()
            .single()
            .points
            .shouldBeNull()
        game.scoreboard.roundPoints(game.roundNumber) shouldBe emptyMap()
    }
}
