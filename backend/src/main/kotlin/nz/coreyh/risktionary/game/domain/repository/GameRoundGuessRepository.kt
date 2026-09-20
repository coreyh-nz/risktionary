package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess

interface GameRoundGuessRepository {
    /** Inserts [guesses], which must be in submission order. */
    fun insertAll(
        roundId: RoundId,
        guesses: List<GameRoundGuess>,
    )
}
