package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType
import nz.coreyh.risktionary.game.socket.messages.view.GameRoundStateView

data class RoundStateEvent(
    val state: GameRoundStateView,
) : RoundEvent(RoundEventType.STATE)
