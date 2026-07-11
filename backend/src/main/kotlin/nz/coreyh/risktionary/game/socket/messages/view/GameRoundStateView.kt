package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType

sealed interface GameRoundStateView {
    val type: RoundStateType

    data object SelectingDrawer : GameRoundStateView {
        override val type: RoundStateType = RoundStateType.SELECTING_DRAWER
    }

    data class InProgress(
        val drawer: GamePlayerView,
        val correctGuessCount: Int,
        val phase: GameRoundPhaseView,
    ) : GameRoundStateView {
        override val type: RoundStateType = RoundStateType.IN_PROGRESS
    }

    data object Completed : GameRoundStateView {
        override val type = RoundStateType.COMPLETED
    }
}

fun GameRoundSession.toStateView(): GameRoundStateView =
    when (val state = this.state) {
        is GameRoundState.SelectingDrawer -> {
            GameRoundStateView.SelectingDrawer
        }

        is GameRoundState.InProgress -> {
            GameRoundStateView.InProgress(
                drawer = state.drawer.toView(),
                correctGuessCount = guesses.getCorrectGuessCount(),
                phase = toPhaseStateView(),
            )
        }

        is GameRoundState.Completed -> {
            GameRoundStateView.Completed
        }
    }
