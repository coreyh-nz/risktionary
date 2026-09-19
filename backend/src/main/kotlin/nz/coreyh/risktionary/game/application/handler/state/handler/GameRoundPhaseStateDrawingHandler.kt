package nz.coreyh.risktionary.game.application.handler.state.handler

import nz.coreyh.risktionary.game.application.handler.state.GameRoundPhaseStateHandler
import nz.coreyh.risktionary.game.application.service.round.GameRoundFeedbackService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionChatService
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.hint.toWordHint
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.game.socket.messages.view.toView
import org.springframework.stereotype.Service

/**
 * Handles entry/exit for the [GameRoundPhase.Drawing] phase.
 */
@Service
class GameRoundPhaseStateDrawingHandler(
    private val gameRoundSessionChatService: GameRoundSessionChatService,
    private val gameRoundFeedbackService: GameRoundFeedbackService,
    private val gameEventPublisher: GameEventPublisher,
) : GameRoundPhaseStateHandler<GameRoundPhase.Drawing> {
    override val phaseClass = GameRoundPhase.Drawing::class

    override fun onEnter(
        round: GameRoundSession,
        state: GameRoundState.InProgress,
        phase: GameRoundPhase.Drawing,
    ) {
        val drawer = state.drawer
        gameRoundSessionChatService.sendMessage(
            round = round,
            message = ChatMessage.System.DrawerSelected(drawer.toView()),
        )

        val game = round.game
        val wordHint = round.word.value.toWordHint()
        gameEventPublisher.publishVolunteersUpdated(game.id, game.volunteers.getVolunteers())
        gameEventPublisher.publishAssignedDrawerEvent(drawer.id, round.word.value)
        gameEventPublisher.publishAssignedGuesserEvent(game.host.id, wordHint)
        game
            .getPlayers()
            .filter { it.id != drawer.id }
            .forEach { gameEventPublisher.publishAssignedGuesserEvent(it.id, wordHint) }
    }

    /**
     * Drawing analysis only matters while the drawing is in progress, so its
     * dispatcher is shut down when the phase ends. That is also when guessing
     * ends, so delayed feedback is generated now.
     */
    override fun onExit(
        round: GameRoundSession,
        phase: GameRoundPhase.Drawing,
    ) {
        round.drawing.close()
        gameRoundFeedbackService.generateDelayed(round)
    }
}
