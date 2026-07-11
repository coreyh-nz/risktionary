package nz.coreyh.risktionary.game.socket.messages.outbound

import nz.coreyh.risktionary.game.socket.messages.event.GameEvent
import nz.coreyh.risktionary.game.socket.messages.event.GameEventType
import nz.coreyh.risktionary.game.socket.messages.view.GameStateView

data class GameStateEvent(
    val state: GameStateView,
) : GameEvent {
    override val type: GameEventType = GameEventType.STATE
}
