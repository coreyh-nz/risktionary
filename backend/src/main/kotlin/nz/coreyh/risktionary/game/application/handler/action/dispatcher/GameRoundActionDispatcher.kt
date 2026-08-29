package nz.coreyh.risktionary.game.application.handler.action.dispatcher

import nz.coreyh.risktionary.game.application.exception.GameActionNotSupportedException
import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.handler.action.GameRoundPhaseActionHandler
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameRoundPhaseStateOrchestrator
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Dispatches a player-submitted action to the
 * [GameRoundPhaseActionHandler] registered for that action's runtime
 * class, but only if the round is currently in the phase that handler
 * expects.
 */
@Service
class GameRoundActionDispatcher(
    private val gameRoundPhaseStateOrchestrator: GameRoundPhaseStateOrchestrator,
    handlers: List<GameRoundPhaseActionHandler<*, *, *>>,
) {
    @Suppress("UNCHECKED_CAST")
    private val handlerMap: Map<KClass<out Any>, GameRoundPhaseActionHandler<GameRoundPhase, Any, Any>> =
        handlers.associateBy { it.actionClass } as Map<KClass<out Any>, GameRoundPhaseActionHandler<GameRoundPhase, Any, Any>>

    /**
     * Resolves and invokes the handler for [action] by its runtime class,
     * throwing if no handler is registered or the round isn't currently in the
     * handler's expected phase.
     */
    fun <A : Any> dispatch(
        round: GameRoundSession,
        player: GamePlayerSession,
        action: A,
    ) {
        val handler = handlerMap[action::class] ?: throw GameActionNotSupportedException()
        val state = round.requireState<GameRoundState.InProgress>()
        val phase = handler.phaseClass.safeCast(state.phase) ?: throw GameRoundStateInvalidException()

        handler.handle(round, phase, player, action)

        if (handler.isPhaseComplete(round.game, round, phase)) {
            gameRoundPhaseStateOrchestrator.advanceOrComplete(round)
        }
    }

    /**
     * Submits [action] to a specific [handler], but only if the round is
     * currently in [handler]'s phase. Returns null instead of throwing when
     * the phase doesn't match, for callers where "not applicable right now" is
     * a normal outcome rather than an error (e.g. chat text that may or may
     * not be a guess attempt).
     */
    fun <P : GameRoundPhase, A : Any, R> submitIfPhaseMatches(
        round: GameRoundSession,
        handler: GameRoundPhaseActionHandler<P, A, R>,
        player: GamePlayerSession,
        action: A,
    ): R? {
        val state = round.state as? GameRoundState.InProgress ?: return null
        val phase = handler.phaseClass.safeCast(state.phase) ?: return null

        val result = handler.handle(round, phase, player, action)
        if (handler.isPhaseComplete(round.game, round, phase)) {
            gameRoundPhaseStateOrchestrator.advanceOrComplete(round)
        }
        return result
    }
}
