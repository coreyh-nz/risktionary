package nz.coreyh.risktionary.game.domain.model.action

import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess

data class GameRoundPhaseGuessAction(
    val guess: String,
)

sealed interface GameRoundPhaseGuessActionResult {
    /** The guess was correct and fully handled; it should not also appear as a chat message. */
    data object Consumed : GameRoundPhaseGuessActionResult

    /**
     * The text was not handled as a guess, so it should be treated as a chat message.
     *
     * [guess] is set when the text was recorded as an incorrect guess, so
     * the chat message can be linked to it.
     */
    data class Skipped(
        val guess: GameRoundGuess? = null,
    ) : GameRoundPhaseGuessActionResult
}
