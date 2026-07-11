package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.domain.model.player.GamePlayer
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

data class GamePlayerView(
    val id: GamePlayerId,
    val displayName: String,
)

fun GamePlayer.toView(): GamePlayerView = GamePlayerView(id = id, displayName = identity.displayName)
