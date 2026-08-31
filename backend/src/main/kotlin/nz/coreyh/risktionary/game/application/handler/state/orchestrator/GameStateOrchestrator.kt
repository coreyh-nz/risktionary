package nz.coreyh.risktionary.game.application.handler.state.orchestrator

import nz.coreyh.risktionary.game.application.handler.state.GameStateHandler
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.domain.model.TimeWindow
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class GameStateOrchestrator(
    private val gameEventPublisher: GameEventPublisher,
    handlers: List<GameStateHandler<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    private val handlerMap =
        handlers.associateBy { it.stateClass } as Map<KClass<out GameState>, GameStateHandler<GameState>>

    fun transitionToStarting(
        session: GameSession,
        timeWindow: TimeWindow,
    ) = transition(session) { session.transitionToStarting(timeWindow) }

    fun transitionToInProgress(session: GameSession) = transition(session) { session.transitionToInProgress() }

    fun transitionToCompleted(session: GameSession) = transition(session) { session.transitionToCompleted() }

    private fun transition(
        session: GameSession,
        mutate: () -> Unit,
    ) {
        val previous = session.state
        handlerMap[previous::class]?.onExit(session, previous)

        mutate()

        val next = session.state
        gameEventPublisher.publishState(session)
        handlerMap[next::class]?.onEnter(session, next)
    }
}
