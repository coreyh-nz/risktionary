package nz.coreyh.risktionary.game.application.handler.action

import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameRoundPhaseStateOrchestrator
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import kotlin.reflect.KClass

/**
 * Reacts to a round entering or leaving a specific [GameRoundPhase].
 *
 * Implementations are invoked by [GameRoundPhaseStateOrchestrator]
 * whenever a round enters or exits the corresponding phase, based on [phaseClass].
 */
interface GameRoundPhaseActionHandler<P : GameRoundPhase, A : Any, R> {
    val phaseClass: KClass<P>
    val actionClass: KClass<A>

    /**
     * Processes [action] submitted by [player] during [phase].
     *
     * @return the result of processing the action.
     */
    fun handle(
        round: GameRoundSession,
        phase: P,
        player: GamePlayerSession,
        action: A,
    ): R

    /**
     * Determines whether [phase] should be considered complete after the most
     * recent action, triggering an advance to the next phase.
     */
    fun isPhaseComplete(
        session: GameSession,
        round: GameRoundSession,
        phase: P,
    ): Boolean
}
