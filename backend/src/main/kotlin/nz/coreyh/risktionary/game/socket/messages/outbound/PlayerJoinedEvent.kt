package nz.coreyh.risktionary.game.socket.messages.outbound

import nz.coreyh.risktionary.game.socket.messages.event.GameEvent
import nz.coreyh.risktionary.game.socket.messages.event.GameEventType
import nz.coreyh.risktionary.game.socket.messages.view.GamePlayerView

/**
 * Event emitted when a new player joins the game.
 */
data class PlayerJoinedEvent(
    val player: GamePlayerView,
) : GameEvent {
    override val type: GameEventType = GameEventType.PLAYER_JOINED
}
