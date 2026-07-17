package nz.coreyh.risktionary.game.application.service.round.phase

import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import kotlin.reflect.KClass

/**
 * Reacts to a round entering or leaving a specific [GameRoundPhase].
 *
 * Implementations are phase-specific and are invoked by
 * [GameRoundPhaseOrchestrator] whenever a round enters or exits the
 * corresponding phase.
 */
interface GameRoundPhaseHandler<P : GameRoundPhase> {
    val phaseClass: KClass<P>

    /**
     * Called once when the round enters this phase.
     */
    fun onEnter(
        round: GameRoundSession,
        state: GameRoundState.InProgress,
        phase: P,
    ) {
    }

    /**
     * Called once when the round leaves this phase.
     */
    fun onExit(
        round: GameRoundSession,
        phase: P,
    ) {
    }
}
