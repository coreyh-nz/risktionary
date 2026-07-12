package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.domain.model.GameStateType
import kotlin.time.Clock

sealed interface GameStateView {
    val type: GameStateType

    data object Lobby : GameStateView {
        override val type: GameStateType = GameStateType.LOBBY
    }

    data class Starting(
        val startingInMs: Long,
    ) : GameStateView {
        override val type: GameStateType = GameStateType.STARTING
    }

    data class InProgress(
        val round: GameRoundStateView,
    ) : GameStateView {
        override val type: GameStateType = GameStateType.IN_PROGRESS
    }

    data object Completed : GameStateView {
        override val type: GameStateType = GameStateType.COMPLETED
    }
}

fun GameSession.toStateView(): GameStateView =
    when (val state = this.state) {
        is GameState.Lobby -> {
            GameStateView.Lobby
        }

        is GameState.Starting -> {
            GameStateView.Starting(
                startingInMs =
                    (state.startingAt - Clock.System.now())
                        .inWholeMilliseconds
                        .coerceAtLeast(0),
            )
        }

        is GameState.InProgress -> {
            GameStateView.InProgress(this.currentRound!!.toStateView())
        }

        is GameState.Completed -> {
            GameStateView.Completed
        }
    }
