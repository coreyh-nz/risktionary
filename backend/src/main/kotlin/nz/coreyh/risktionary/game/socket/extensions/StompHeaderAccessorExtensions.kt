package nz.coreyh.risktionary.game.socket.extensions

import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import org.springframework.messaging.simp.stomp.StompHeaderAccessor

val StompHeaderAccessor.gameId: GameId
    get() =
        (sessionAttributes?.get("gameId") as? GameId)
            ?: throw IllegalStateException("gameId not found in STOMP session attributes")

val StompHeaderAccessor.playerId: GamePlayerId
    get() =
        (sessionAttributes?.get("playerId") as? GamePlayerId)
            ?: throw IllegalStateException("playerId not found in STOMP session attributes")
