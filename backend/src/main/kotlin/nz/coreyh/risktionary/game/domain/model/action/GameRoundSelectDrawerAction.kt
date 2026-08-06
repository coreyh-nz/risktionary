package nz.coreyh.risktionary.game.domain.model.action

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

data class GameRoundSelectDrawerAction(
    val drawerId: GamePlayerId,
)
