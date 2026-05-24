package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameRoundSessionGuessService(
    private val gameEventPublisher: GameEventPublisher,
) {
    fun handleGuess(
        round: GameRoundSession,
        player: GamePlayerSession,
        text: String,
    ): GuessResultType {
        val word = round.word
        val result =
            if (
                word.value.equals(text, ignoreCase = true) ||
                word.synonyms.any { it.equals(text, ignoreCase = true) }
            ) {
                GuessResultType.CORRECT
            } else {
                GuessResultType.INCORRECT
            }
        round.guesses.recordGuess(player.id, text, result)

        if (result == GuessResultType.CORRECT) {
            val correctCount = round.guesses.getCorrectGuessCount()
            gameEventPublisher.publishRoundCorrectGuessesCountUpdated(
                gameId = round.gameId,
                correctGuesses = correctCount,
            )
            gameEventPublisher.publishRoundCorrectGuess(
                playerId = player.id,
                word = round.word,
            )
        }
        return result
    }
}
