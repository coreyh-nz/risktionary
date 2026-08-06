package nz.coreyh.risktionary.game.socket.controller

import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameActionDispatcher
import nz.coreyh.risktionary.game.domain.model.action.GameUnvolunteerAction
import nz.coreyh.risktionary.game.domain.model.action.GameVolunteerAction
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketMappings
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameVolunteerSocketController(
    private val gameActionDispatcher: GameActionDispatcher,
) {
    @MessageMapping(WebSocketMappings.VOLUNTEER)
    fun onVolunteer(principal: GameSocketPrincipal) {
        if (principal !is GameSocketPrincipal.Player) return

        gameActionDispatcher.dispatch(
            gameId = principal.gameId,
            playerId = principal.id,
            action = GameVolunteerAction,
        )
    }

    @MessageMapping(WebSocketMappings.UNVOLUNTEER)
    fun onUnvolunteer(principal: GameSocketPrincipal) {
        if (principal !is GameSocketPrincipal.Player) return

        gameActionDispatcher.dispatch(
            gameId = principal.gameId,
            playerId = principal.id,
            action = GameUnvolunteerAction,
        )
    }
}
