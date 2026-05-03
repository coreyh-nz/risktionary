package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

data class GamePlayerView(
    val id: GamePlayerId,
    val displayName: String,
)

fun GamePlayerSession.toView() = GamePlayerView(id = id, displayName = identity.displayName)
