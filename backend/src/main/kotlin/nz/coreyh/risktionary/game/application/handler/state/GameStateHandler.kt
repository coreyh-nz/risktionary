package nz.coreyh.risktionary.game.application.handler.state

import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameStateOrchestrator
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameState
import kotlin.reflect.KClass

/**
 * Reacts to a game session entering or leaving a specific [GameState].
 *
 * Implementations are invoked by [GameStateOrchestrator] whenever a
 * session transitions state, based on [stateClass].
 */
interface GameStateHandler<S : GameState> {
    val stateClass: KClass<S>

    /**
     * Called once when the game enters this state.
     */
    fun onEnter(
        session: GameSession,
        state: S,
    ) {
    }

    /**
     * Called once when the game leaves this state.
     */
    fun onExit(
        session: GameSession,
        state: GameState,
    ) {
    }
}
