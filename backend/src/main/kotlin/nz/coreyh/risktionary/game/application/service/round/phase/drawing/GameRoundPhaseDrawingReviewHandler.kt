package nz.coreyh.risktionary.game.application.service.round.phase.drawing

import nz.coreyh.risktionary.game.application.service.round.phase.GameRoundPhaseHandler
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

/**
 * Handles entry/exit for the [GameRoundPhase.DrawingReview] phase.
 *
 * If the game's configuration specifies a duration for this phase,
 * schedules an automatic advance once the timer expires.
 */
@Service
class GameRoundPhaseDrawingReviewHandler(
    private val gameEventPublisher: GameEventPublisher,
) : GameRoundPhaseHandler<GameRoundPhase.DrawingReview> {
    override val phaseClass = GameRoundPhase.DrawingReview::class

    override fun onEnter(
        round: GameRoundSession,
        state: GameRoundState.InProgress,
        phase: GameRoundPhase.DrawingReview,
    ) {
        val activeGuessingPlayerIds =
            round.game
                .getActivePlayers()
                .filter { it.id != state.drawer.id }
                .map { it.id }
        val allGuessedCorrectly = round.guesses.hasAllGuessedCorrectly(activeGuessingPlayerIds)

        when (allGuessedCorrectly) {
            true -> {
                gameEventPublisher.publishRoundChatMessage(
                    gameId = round.game.id,
                    message = ChatMessage.System.DrawingEndedAllGuessed(round.word.value),
                )
            }

            false -> {
                gameEventPublisher.publishRoundChatMessage(
                    gameId = round.game.id,
                    message = ChatMessage.System.DrawingEndedTimeUp(round.word.value),
                )
            }
        }
    }
}
