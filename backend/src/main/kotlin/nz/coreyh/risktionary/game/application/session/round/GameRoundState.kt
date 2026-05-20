package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType

sealed interface GameRoundState {
    val type: RoundStateType

    data object SelectingDrawer : GameRoundState {
        override val type = RoundStateType.SELECTING_DRAWER
    }

    data class InProgress(
        val drawerId: GamePlayerId,
    ) : GameRoundState {
        override val type = RoundStateType.IN_PROGRESS
    }

    data object Completed : GameRoundState {
        override val type = RoundStateType.COMPLETED
    }
}
