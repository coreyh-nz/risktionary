package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType

data class RoundCorrectGuessesCountUpdatedEvent(
    val correctGuesses: Int,
) : RoundEvent(RoundEventType.CORRECT_GUESSES_COUNT)
