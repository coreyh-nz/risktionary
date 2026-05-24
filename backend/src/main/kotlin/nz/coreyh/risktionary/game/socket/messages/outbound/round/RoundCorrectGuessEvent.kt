package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType

data class RoundCorrectGuessEvent(
    val word: String,
) : RoundEvent(RoundEventType.CORRECT_GUESS)
