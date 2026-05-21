package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.socket.messages.event.GameEvent
import nz.coreyh.risktionary.game.socket.messages.event.GameEventType

data class RoundStateChangedEvent(
    val state: GameRoundState,
) : GameEvent(GameEventType.ROUND_STATE_CHANGED)
