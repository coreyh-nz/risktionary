package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameRoundSessionGuessService(
    private val gameRoundSessionService: GameRoundSessionService,
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
                gameId = round.game.id,
                correctGuesses = correctCount,
            )
            gameEventPublisher.publishRoundCorrectGuess(
                playerId = player.id,
                word = round.word,
            )
        }
        return result
    }

    fun checkRoundCompletion(
        session: GameSession,
        round: GameRoundSession,
    ) {
        // transition to review if all players have guessed correctly
        val correctCount = round.guesses.getCorrectGuessCount()
        val guesserCount =
            session.getPlayers().count {
                it.id != round.requireState<GameRoundState.InProgress>().drawer.id
            }
        if (correctCount >= guesserCount) {
            gameRoundSessionService.transitionToDrawingReviewAllGuessed(round)
        }
    }
}
