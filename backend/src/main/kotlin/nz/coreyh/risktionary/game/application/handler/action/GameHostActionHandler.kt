package nz.coreyh.risktionary.game.application.handler.action

import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameHostActionDispatcher
import nz.coreyh.risktionary.game.application.session.GameSession
import kotlin.reflect.KClass

/**
 * Handles a host-submitted action at the session level.
 *
 * Implementations are resolved by [GameHostActionDispatcher] based on
 * [actionClass], so each action type should have exactly one handler.
 */
interface GameHostActionHandler<A : Any> {
    val actionClass: KClass<A>

    fun handle(
        session: GameSession,
        action: A,
    )
}
