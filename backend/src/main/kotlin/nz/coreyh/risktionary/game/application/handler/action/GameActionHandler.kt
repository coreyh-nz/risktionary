package nz.coreyh.risktionary.game.application.handler.action

import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameActionDispatcher
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import kotlin.reflect.KClass

/**
 * Handles a player-submitted action at the session level.
 *
 * Implementations are resolved by [GameActionDispatcher] based on
 * [actionClass], so each action type should have exactly one handler.
 */
interface GameActionHandler<A : Any> {
    val actionClass: KClass<A>

    fun handle(
        session: GameSession,
        player: GamePlayerSession,
        action: A,
    )
}
