package nz.coreyh.risktionary.game.application.handler.action.handler

import nz.coreyh.risktionary.game.application.handler.action.GameRoundPhaseActionHandler
import nz.coreyh.risktionary.game.application.service.round.GameRoundFeedbackService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionChatService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.action.GameRoundPhaseGuessAction
import nz.coreyh.risktionary.game.domain.model.action.GameRoundPhaseGuessActionResult
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.game.socket.messages.view.toView
import org.springframework.stereotype.Service

/**
 * Handles a player's guess submitted during the [GameRoundPhase.Drawing]
 * phase.
 *
 * The phase is complete once every non-drawer player has guessed
 * correctly.
 */
@Service
class GameRoundPhaseGuessActionHandler(
    private val gameRoundSessionChatService: GameRoundSessionChatService,
    private val gameEventPublisher: GameEventPublisher,
    private val gameRoundFeedbackService: GameRoundFeedbackService,
) : GameRoundPhaseActionHandler<GameRoundPhase.Drawing, GameRoundPhaseGuessAction, GameRoundPhaseGuessActionResult> {
    override val phaseClass = GameRoundPhase.Drawing::class
    override val actionClass = GameRoundPhaseGuessAction::class

    override fun handle(
        round: GameRoundSession,
        phase: GameRoundPhase.Drawing,
        player: GamePlayerSession,
        action: GameRoundPhaseGuessAction,
    ): GameRoundPhaseGuessActionResult {
        val roundState = round.requireState<GameRoundState.InProgress>()
        val drawerId = roundState.drawer.id
        val hasCorrectlyGuessed = round.guesses.hasGuessedCorrectly(player.id)
        val isDrawer = drawerId == player.id

        val isGuessAttempt =
            !isDrawer &&
                !hasCorrectlyGuessed &&
                roundState.phase is GameRoundPhase.Drawing

        if (!isGuessAttempt) return GameRoundPhaseGuessActionResult.Skipped()

        val guess = action.guess
        val word = round.word
        val result =
            when (
                word.value.equals(guess, ignoreCase = true) ||
                    word.synonyms.any { it.equals(guess, ignoreCase = true) }
            ) {
                true -> GuessResultType.CORRECT
                false -> GuessResultType.INCORRECT
            }

        val recordedGuess = round.guesses.recordGuess(player.id, guess, result)

        if (result == GuessResultType.CORRECT) {
            gameEventPublisher.publishRoundCorrectGuessesCountUpdated(
                gameId = round.game.id,
                correctGuesses = round.guesses.getCorrectGuessCount(),
            )
            gameEventPublisher.publishRoundCorrectGuess(
                playerId = player.id,
                word = round.word,
            )
            val message =
                gameRoundSessionChatService.sendMessage(
                    round,
                    ChatMessage.System.CorrectGuess(
                        player = player.player.toView(),
                    ),
                )
            gameRoundFeedbackService.onGuess(round, player, recordedGuess, message.id)
            return GameRoundPhaseGuessActionResult.Consumed
        }

        // incorrect guesses are shown as chat messages, so their feedback is handled with the message
        return GameRoundPhaseGuessActionResult.Skipped(recordedGuess)
    }

    override fun isPhaseComplete(
        session: GameSession,
        round: GameRoundSession,
        phase: GameRoundPhase.Drawing,
    ): Boolean {
        val drawerId = round.requireState<GameRoundState.InProgress>().drawer.id
        val activeGuessingPlayerIds =
            round.game
                .getActivePlayers()
                .filter { it.id != drawerId }
                .map { it.id }
        return round.guesses.hasAllGuessedCorrectly(activeGuessingPlayerIds)
    }
}
