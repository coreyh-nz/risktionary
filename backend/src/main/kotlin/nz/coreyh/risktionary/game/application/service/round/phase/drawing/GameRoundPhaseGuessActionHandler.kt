package nz.coreyh.risktionary.game.application.service.round.phase.drawing

import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionChatService
import nz.coreyh.risktionary.game.application.service.round.phase.GameRoundPhaseActionHandler
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
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
) : GameRoundPhaseActionHandler<GameRoundPhase.Drawing, String, Boolean> {
    override val phaseClass = GameRoundPhase.Drawing::class

    /**
     * @returns `true` if the message is a correct guess, `false` otherwise
     */
    override fun handle(
        round: GameRoundSession,
        phase: GameRoundPhase.Drawing,
        player: GamePlayerSession,
        action: String,
    ): Boolean {
        val roundState = round.requireState<GameRoundState.InProgress>()
        val drawerId = roundState.drawer.id
        val hasCorrectlyGuessed = round.guesses.hasGuessedCorrectly(player.id)
        val isDrawer = drawerId == player.id

        val isGuessAttempt =
            !isDrawer &&
                !hasCorrectlyGuessed &&
                roundState.phase is GameRoundPhase.Drawing

        // not a guess attempt so do not attempt to handle
        if (!isGuessAttempt) return false

        val word = round.word
        val result =
            if (
                word.value.equals(action, ignoreCase = true) ||
                word.synonyms.any { it.equals(action, ignoreCase = true) }
            ) {
                GuessResultType.CORRECT
            } else {
                GuessResultType.INCORRECT
            }

        round.guesses.recordGuess(player.id, action, result)

        if (result == GuessResultType.CORRECT) {
            gameEventPublisher.publishRoundCorrectGuessesCountUpdated(
                gameId = round.game.id,
                correctGuesses = round.guesses.getCorrectGuessCount(),
            )
            gameEventPublisher.publishRoundCorrectGuess(
                playerId = player.id,
                word = round.word,
            )
            gameRoundSessionChatService.sendMessage(
                round,
                ChatMessage.System.CorrectGuess(
                    player = player.player.toView(),
                ),
            )
            return true
        }

        return false
    }

    override fun isPhaseComplete(
        session: GameSession,
        round: GameRoundSession,
        phase: GameRoundPhase.Drawing,
    ): Boolean {
        val drawerId = round.requireState<GameRoundState.InProgress>().drawer.id
        val guesserCount = session.getPlayers().count { it.id != drawerId }
        return round.guesses.getCorrectGuessCount() >= guesserCount
    }
}
