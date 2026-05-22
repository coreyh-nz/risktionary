package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.domain.model.round.hint.WordHint
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType

data class RoundAssignedGuesserEvent(
    val hint: WordHint,
) : RoundEvent(RoundEventType.ASSIGNED_GUESSER)
