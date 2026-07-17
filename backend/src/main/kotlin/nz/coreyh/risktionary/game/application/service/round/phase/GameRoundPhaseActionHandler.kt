package nz.coreyh.risktionary.game.application.service.round.phase

import nz.coreyh.risktionary.game.application.service.round.GameRoundActionCoordinator
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import kotlin.reflect.KClass

/**
 * Handles a player-submitted action during a specific [GameRoundPhase].
 *
 * Implementations are phase-specific and are invoked via
 * [GameRoundActionCoordinator], which resolves the correct handler
 * and checks for phase completion after each submitted action.
 */
interface GameRoundPhaseActionHandler<P : GameRoundPhase, A, R> {
    val phaseClass: KClass<P>

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
