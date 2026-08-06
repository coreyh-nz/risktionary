package nz.coreyh.risktionary.game.application.handler.action.dispatcher

import nz.coreyh.risktionary.game.application.exception.GameActionNotSupportedException
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.handler.action.GameHostActionHandler
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.user.domain.model.UserId
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

/**
 * Entry point for dispatching a host-submitted action to the correct
 * [GameHostActionHandler].
 */
@Service
class GameHostActionDispatcher(
    private val gameSessionStore: GameSessionStore,
    handlers: List<GameHostActionHandler<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    private val handlerMap: Map<KClass<out Any>, GameHostActionHandler<Any>> =
        handlers.associateBy { it.actionClass } as Map<KClass<out Any>, GameHostActionHandler<Any>>

    /**
     * Dispatches [action] on behalf of the host identified by [hostId] in
     * [gameId].
     */
    fun <A : Any> dispatch(
        gameId: GameId,
        hostId: UserId,
        action: A,
    ) {
        val session = gameSessionStore.findById(gameId) ?: throw GameNotFoundException()
        if (session.host.id != hostId) throw GameNotFoundException()

        val handler = handlerMap[action::class] ?: throw GameActionNotSupportedException()
        handler.handle(session, action)
    }
}
