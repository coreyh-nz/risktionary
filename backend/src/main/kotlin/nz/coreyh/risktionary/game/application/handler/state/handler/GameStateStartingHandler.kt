package nz.coreyh.risktionary.game.application.handler.state.handler

import nz.coreyh.risktionary.game.application.handler.state.GameStateHandler
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameStateOrchestrator
import nz.coreyh.risktionary.game.application.service.GameSessionTaskService
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameState
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.seconds

@Service
class GameStateStartingHandler(
    private val gameSessionTaskService: GameSessionTaskService,
    private val gameStateOrchestrator: ObjectProvider<GameStateOrchestrator>,
) : GameStateHandler<GameState.Starting> {
    override val stateClass = GameState.Starting::class

    override fun onEnter(
        session: GameSession,
        state: GameState.Starting,
    ) {
        val timeWindow = state.timeWindow
        val block = { gameStateOrchestrator.getObject().transitionToInProgress(session) }

        if (timeWindow.duration >= 0.seconds) {
            gameSessionTaskService.schedule(session.id, timeWindow.endingAt, block)
        } else {
            block()
        }
    }
}
