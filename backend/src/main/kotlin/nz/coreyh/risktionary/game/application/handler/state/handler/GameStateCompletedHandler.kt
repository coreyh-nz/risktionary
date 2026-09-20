package nz.coreyh.risktionary.game.application.handler.state.handler

import nz.coreyh.risktionary.game.application.handler.state.GameStateHandler
import nz.coreyh.risktionary.game.application.service.GameResearchPersistenceService
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.game.domain.model.GameState
import org.springframework.stereotype.Service

/**
 * Records that the game finished when it enters [GameState.Completed].
 */
@Service
class GameStateCompletedHandler(
    private val gameResearchPersistenceService: GameResearchPersistenceService,
) : GameStateHandler<GameState.Completed> {
    override val stateClass = GameState.Completed::class

    override fun onEnter(
        session: GameSession,
        state: GameState.Completed,
    ) {
        gameResearchPersistenceService.markGameEnded(session, GameEndReason.COMPLETED)
    }
}
