package nz.coreyh.risktionary.game.application.handler.action.handler.host

import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.application.handler.action.GameHostActionHandler
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameRoundPhaseStateOrchestrator
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhaseStateTransitionService
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.action.GameRoundSelectDrawerAction
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameRoundSelectDrawerActionHandler(
    private val gameRoundPhaseStateTransitionService: GameRoundPhaseStateTransitionService,
    private val gameRoundPhaseStateOrchestrator: GameRoundPhaseStateOrchestrator,
    private val gameEventPublisher: GameEventPublisher,
) : GameHostActionHandler<GameRoundSelectDrawerAction> {
    override val actionClass = GameRoundSelectDrawerAction::class

    override fun handle(
        session: GameSession,
        action: GameRoundSelectDrawerAction,
    ) {
        val drawer = session.getPlayer(action.drawerId)
        val round = session.selectDrawer(drawer)
        gameEventPublisher.publishVolunteersUpdated(session.id, session.volunteers.getVolunteers())

        val phase =
            gameRoundPhaseStateTransitionService.next(round, round.requireState<GameRoundState.InProgress>().phase)
                ?: throw GameStateInvalidException()
        round.updatePhase(phase)
        gameRoundPhaseStateOrchestrator.enterInitialPhase(round, phase)
    }
}
