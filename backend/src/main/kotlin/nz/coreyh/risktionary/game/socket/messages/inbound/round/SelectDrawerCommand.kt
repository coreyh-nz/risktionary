package nz.coreyh.risktionary.game.socket.messages.inbound.round

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

data class SelectDrawerCommand(
    val drawerId: GamePlayerId,
)
