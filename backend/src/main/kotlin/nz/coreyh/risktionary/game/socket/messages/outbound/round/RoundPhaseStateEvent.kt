package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType
import nz.coreyh.risktionary.game.socket.messages.view.GameRoundPhaseView

data class RoundPhaseStateEvent(
    val phase: GameRoundPhaseView,
) : RoundEvent(RoundEventType.PHASE_STATE)
