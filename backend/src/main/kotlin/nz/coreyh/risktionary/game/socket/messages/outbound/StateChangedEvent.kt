package nz.coreyh.risktionary.game.socket.messages.outbound

import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.socket.messages.event.GameEvent
import nz.coreyh.risktionary.game.socket.messages.event.GameEventType

data class StateChangedEvent(
    val state: GameState,
) : GameEvent(GameEventType.STATE_CHANGED)
