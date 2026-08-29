package nz.coreyh.risktionary.game.application.handler.state.orchestrator

import nz.coreyh.risktionary.game.application.handler.state.GameRoundPhaseStateHandler
import nz.coreyh.risktionary.game.application.service.GameSessionTaskService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhaseStateTransitionService
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

/**
 * Drives a round's progression between [GameRoundPhase]s.
 *
 * This is the single place that mutates a round's current phase - storing
 * the new phase, publishing the appropriate event, notifying the relevant
 * [GameRoundPhaseStateHandler], and scheduling an automatic advance if the
 * phase has a configured duration.
 */
@Service
class GameRoundPhaseStateOrchestrator(
    handlers: List<GameRoundPhaseStateHandler<*>>,
    private val gameRoundSessionService: GameRoundSessionService,
    private val gameSessionTaskService: GameSessionTaskService,
    private val gameRoundPhaseStateTransitionService: GameRoundPhaseStateTransitionService,
    private val gameEventPublisher: GameEventPublisher,
) {
    @Suppress("UNCHECKED_CAST")
    private val handlerMap: Map<KClass<out GameRoundPhase>, GameRoundPhaseStateHandler<GameRoundPhase>> =
        handlers.associateBy { it.phaseClass } as Map<KClass<out GameRoundPhase>, GameRoundPhaseStateHandler<GameRoundPhase>>

    /**
     * Enters [phase] as the round's first phase, with no previous phase to
     * exit. Publishes the full round state, since this is the client's first
     * view of the round being in progress.
     */
    fun enterInitialPhase(
        round: GameRoundSession,
        phase: GameRoundPhase,
    ) {
        enter(round, phase, publishAsRoundState = true)
    }

    /**
     * Transitions the round from its current phase to [next], calling `onExit`
     * for the current phase before entering [next].
     */
    fun transitionTo(
        round: GameRoundSession,
        next: GameRoundPhase,
    ) {
        val previous = round.requireState<GameRoundState.InProgress>().phase
        handlerMap[previous::class]?.onExit(round, previous)
        enter(round, next, publishAsRoundState = false)
    }

    /**
     * Advances to the next phase, or completes the round if the plan is
     * exhausted.
     */
    fun advanceOrComplete(round: GameRoundSession) {
        val phase = round.requireState<GameRoundState.InProgress>().phase
        val next = gameRoundPhaseStateTransitionService.next(round, phase)
        if (next != null) {
            transitionTo(round, next)
        } else {
            gameRoundSessionService.completeRound(round)
        }
    }

    /**
     * Stores [phase] on the round, publishes the corresponding event, invokes
     * the phase's `onEnter`, and schedules an automatic advance if [phase] has
     * a configured end time.
     *
     * @param publishAsRoundState `true` for a round's first phase (full round
     *    state), `false` for subsequent transitions (just the phase change).
     */
    private fun enter(
        round: GameRoundSession,
        phase: GameRoundPhase,
        publishAsRoundState: Boolean,
    ) {
        round.updatePhase(phase)

        if (publishAsRoundState) {
            gameEventPublisher.publishRoundState(round)
        } else {
            gameEventPublisher.publishRoundPhaseState(round)
        }

        val state = round.requireState<GameRoundState.InProgress>()
        handlerMap[phase::class]?.onEnter(round, state, phase)

        val endingAt = phase.timeWindow?.endingAt ?: return
        gameSessionTaskService.schedule(round.game.id, endingAt) {
            val currentState = round.state
            if (currentState is GameRoundState.InProgress && currentState.phase.type == phase.type) {
                advanceOrComplete(round)
            }
        }
    }
}
