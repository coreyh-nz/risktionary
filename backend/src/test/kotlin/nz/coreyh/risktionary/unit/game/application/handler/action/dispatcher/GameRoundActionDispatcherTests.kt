package nz.coreyh.risktionary.unit.game.application.handler.action.dispatcher

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import nz.coreyh.risktionary.game.application.exception.GameActionNotSupportedException
import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.handler.action.GameRoundPhaseActionHandler
import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameRoundActionDispatcher
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameRoundPhaseStateOrchestrator
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.extensions.mockkRelaxed
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@MockKTest
class GameRoundActionDispatcherTests {
    private lateinit var gameRoundPhaseStateOrchestrator: GameRoundPhaseStateOrchestrator
    private val round = mockkRelaxed<GameRoundSession>()
    private val player = mockkRelaxed<GamePlayerSession>()
    private val action = GameRoundTestAction()

    @BeforeEach
    fun setup() {
        gameRoundPhaseStateOrchestrator = mockkRelaxed()
    }

    @Nested
    inner class Dispatch {
        @Test
        fun `dispatch dispatches action to matching phase handler`() {
            val phase = mockkRelaxed<GameRoundPhase.Drawing>()
            val handler = mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestAction, Unit>>()
            every { handler.actionClass } returns GameRoundTestAction::class
            every { handler.phaseClass } returns GameRoundPhase.Drawing::class
            every { round.state } returns
                GameRoundState.InProgress(
                    phase = phase,
                    drawer = player.player,
                )
            every { handler.isPhaseComplete(round.game, round, phase) } returns false

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            gameRoundActionDispatcher.dispatch(round, player, action)

            verify {
                handler.handle(round, phase, player, action)
            }
        }

        @Test
        fun `dispatch advances round when handler completes phase`() {
            val phase = mockkRelaxed<GameRoundPhase.Drawing>()
            val handler = mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestAction, Unit>>()
            every { handler.actionClass } returns GameRoundTestAction::class
            every { handler.phaseClass } returns GameRoundPhase.Drawing::class
            every { round.state } returns
                GameRoundState.InProgress(
                    phase = phase,
                    drawer = player.player,
                )
            every { handler.isPhaseComplete(round.game, round, phase) } returns true

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            gameRoundActionDispatcher.dispatch(round, player, action)

            verify {
                handler.handle(round, phase, player, action)
                gameRoundPhaseStateOrchestrator.advanceOrComplete(round)
            }
        }

        @Test
        fun `dispatch throws game action not supported exception when action has no matching handler`() {
            class GameRoundTestDifferentAction

            val handler =
                mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestDifferentAction, Unit>>()
            every { handler.actionClass } returns GameRoundTestDifferentAction::class
            every { handler.phaseClass } returns GameRoundPhase.Drawing::class

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            shouldThrow<GameActionNotSupportedException> { gameRoundActionDispatcher.dispatch(round, player, action) }

            verify(exactly = 0) {
                handler.handle(any(), any(), any(), any())
                gameRoundPhaseStateOrchestrator.advanceOrComplete(any())
            }
        }

        @Test
        fun `dispatch throws game round state invalid exception when round is in different phase`() {
            val phase = mockkRelaxed<GameRoundPhase.Ranking>()
            val handler = mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestAction, Unit>>()
            every { handler.actionClass } returns GameRoundTestAction::class
            every { handler.phaseClass } returns GameRoundPhase.Drawing::class
            every { round.state } returns
                GameRoundState.InProgress(
                    phase = phase,
                    drawer = player.player,
                )

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            shouldThrow<GameRoundStateInvalidException> { gameRoundActionDispatcher.dispatch(round, player, action) }

            verify(exactly = 0) {
                handler.handle(any(), any(), any(), any())
                gameRoundPhaseStateOrchestrator.advanceOrComplete(any())
            }
        }
    }

    @Nested
    inner class SubmitIfPhaseMatches {
        @Test
        fun `submit if phase matches submits action to handler and returns result`() {
            val phase = mockkRelaxed<GameRoundPhase.Drawing>()
            val result = "result"
            val handler =
                mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestAction, String>>()
            every { round.state } returns
                GameRoundState.InProgress(
                    phase = phase,
                    drawer = player.player,
                )
            every { handler.phaseClass } returns GameRoundPhase.Drawing::class
            every { handler.handle(round, phase, player, action) } returns result
            every { handler.isPhaseComplete(round.game, round, phase) } returns false

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            val actual = gameRoundActionDispatcher.submitIfPhaseMatches(round, handler, player, action)

            actual shouldBe result
            verify {
                handler.handle(round, phase, player, action)
            }
        }

        @Test
        fun `submit if phase matches advances round when handler completes phase`() {
            val phase = mockkRelaxed<GameRoundPhase.Drawing>()
            val handler = mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestAction, Unit>>()
            every { round.state } returns
                GameRoundState.InProgress(
                    phase = phase,
                    drawer = player.player,
                )
            every { handler.phaseClass } returns GameRoundPhase.Drawing::class
            every { handler.isPhaseComplete(round.game, round, phase) } returns true

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            gameRoundActionDispatcher.submitIfPhaseMatches(round, handler, player, action)

            verify {
                handler.handle(round, phase, player, action)
                gameRoundPhaseStateOrchestrator.advanceOrComplete(round)
            }
        }

        @Test
        fun `submit throws game round state invalid exception when round is not in progress`() {
            val handler = mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestAction, Unit>>()
            every { round.state } returns GameRoundState.SelectingDrawer
            every { handler.actionClass } returns GameRoundTestAction::class

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            val result = gameRoundActionDispatcher.submitIfPhaseMatches(round, handler, player, action)

            result shouldBe null
            verify(exactly = 0) {
                handler.handle(any(), any(), any(), any())
                gameRoundPhaseStateOrchestrator.advanceOrComplete(any())
            }
        }

        @Test
        fun `submit if phase matches returns null when round is in different phase`() {
            val phase = mockkRelaxed<GameRoundPhase.Ranking>()
            val handler = mockkRelaxed<GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundTestAction, Unit>>()
            every { round.state } returns
                GameRoundState.InProgress(
                    phase = phase,
                    drawer = player.player,
                )
            every { handler.phaseClass } returns GameRoundPhase.Drawing::class

            val gameRoundActionDispatcher = gameRoundActionDispatcher(handler)
            val result = gameRoundActionDispatcher.submitIfPhaseMatches(round, handler, player, action)

            result shouldBe null
            verify(exactly = 0) {
                handler.handle(any(), any(), any(), any())
                gameRoundPhaseStateOrchestrator.advanceOrComplete(any())
            }
        }
    }

    private class GameRoundTestAction

    private fun gameRoundActionDispatcher(vararg handlers: GameRoundPhaseActionHandler<*, *, *>) =
        GameRoundActionDispatcher(
            gameRoundPhaseStateOrchestrator = gameRoundPhaseStateOrchestrator,
            handlers = handlers.toList(),
        )
}
