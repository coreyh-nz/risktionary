package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.service.round.phase.GameRoundPhaseActionHandler
import nz.coreyh.risktionary.game.application.service.round.phase.GameRoundPhaseOrchestrator
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import org.springframework.stereotype.Service
import kotlin.reflect.safeCast

/**
 * Entry point for submitting a player action against the round's current
 * phase.
 *
 * Resolves the phase from the round's current state, delegates to a
 * [GameRoundPhaseActionHandler] to process the action, and advances the
 * round if the handler reports the phase as complete.
 */
@Service
class GameRoundActionCoordinator(
    private val orchestrator: GameRoundPhaseOrchestrator,
) {
    /**
     * Submits [action] from [player] to [handler].
     *
     * @throws GameRoundStateInvalidException if the round's current phase does
     *    not match [handler]'s [GameRoundPhaseActionHandler.phaseClass].
     */
    fun <P : GameRoundPhase, A, R> submit(
        round: GameRoundSession,
        handler: GameRoundPhaseActionHandler<P, A, R>,
        player: GamePlayerSession,
        action: A,
    ): R {
        val state = round.requireState<GameRoundState.InProgress>()
        val phase =
            handler.phaseClass.safeCast(state.phase)
                ?: throw GameRoundStateInvalidException()

        val result = handler.handle(round, phase, player, action)

        if (handler.isPhaseComplete(round.game, round, phase)) {
            orchestrator.advanceOrComplete(round)
        }

        return result
    }
}
