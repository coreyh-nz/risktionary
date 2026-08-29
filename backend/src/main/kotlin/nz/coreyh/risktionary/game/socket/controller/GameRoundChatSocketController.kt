package nz.coreyh.risktionary.game.socket.controller

import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameActionDispatcher
import nz.coreyh.risktionary.game.domain.model.action.GameRoundChatAction
import nz.coreyh.risktionary.game.socket.messages.inbound.round.ChatMessageCommand
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketMappings
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameRoundChatSocketController(
    private val gameActionDispatcher: GameActionDispatcher,
) {
    @MessageMapping(WebSocketMappings.CHAT)
    fun onChatMessage(
        principal: GameSocketPrincipal,
        command: ChatMessageCommand,
    ) {
        if (principal !is GameSocketPrincipal.Player) return

        gameActionDispatcher.dispatch(
            gameId = principal.gameId,
            playerId = principal.id,
            action = GameRoundChatAction(command.text),
        )
    }
}
