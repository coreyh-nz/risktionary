package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType

data class RoundStateChangedEvent(
    val state: GameRoundState,
) : RoundEvent(RoundEventType.ROUND_STATE_CHANGED)
