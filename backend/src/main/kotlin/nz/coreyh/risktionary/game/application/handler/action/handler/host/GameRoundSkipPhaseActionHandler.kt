package nz.coreyh.risktionary.game.application.handler.action.handler.host

import nz.coreyh.risktionary.game.application.handler.action.GameHostActionHandler
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameRoundPhaseStateOrchestrator
import nz.coreyh.risktionary.game.application.service.GameSessionTaskService
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.requireActiveRound
import nz.coreyh.risktionary.game.domain.model.action.GameRoundSkipPhaseAction
import org.springframework.stereotype.Service

@Service
class GameRoundSkipPhaseActionHandler(
    private val gameSessionTaskService: GameSessionTaskService,
    private val gameRoundPhaseStateOrchestrator: GameRoundPhaseStateOrchestrator,
) : GameHostActionHandler<GameRoundSkipPhaseAction> {
    override val actionClass = GameRoundSkipPhaseAction::class

    override fun handle(
        session: GameSession,
        action: GameRoundSkipPhaseAction,
    ) {
        if (!session.config.skippingCountdownsEnabled) return

        val round = session.requireActiveRound()
        gameSessionTaskService.cancelAll(round.game.id)
        gameRoundPhaseStateOrchestrator.advanceOrComplete(round)
    }
}
