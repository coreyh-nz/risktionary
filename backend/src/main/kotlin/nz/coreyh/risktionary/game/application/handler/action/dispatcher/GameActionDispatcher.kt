package nz.coreyh.risktionary.game.application.handler.action.dispatcher

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.handler.action.GameActionHandler
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

/**
 * Entry point for dispatching a player-submitted action to the correct
 * handler.
 */
@Service
class GameActionDispatcher(
    private val gameSessionStore: GameSessionStore,
    private val gameRoundActionDispatcher: GameRoundActionDispatcher,
    sessionHandlers: List<GameActionHandler<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    private val sessionHandlerMap: Map<KClass<out Any>, GameActionHandler<Any>> =
        sessionHandlers.associateBy { it.actionClass } as Map<KClass<out Any>, GameActionHandler<Any>>

    /**
     * Dispatches [action] for [playerId] in [gameId].
     *
     * Tries a session-scoped [GameActionHandler] first; if none matches the
     * action's runtime class, delegates to [GameRoundActionDispatcher] for the
     * game's current round.
     */
    fun <A : Any> dispatch(
        gameId: GameId,
        playerId: GamePlayerId,
        action: A,
    ) {
        val session = gameSessionStore.findById(gameId) ?: throw GameNotFoundException()
        val player = session.findPlayer(playerId) ?: throw GamePlayerNotInSessionException()

        val sessionHandler = sessionHandlerMap[action::class]
        if (sessionHandler != null) {
            sessionHandler.handle(session, player, action)
            return
        }

        val round = session.currentRound ?: throw GameRoundStateInvalidException()
        gameRoundActionDispatcher.dispatch(round, player, action)
    }
}
