package nz.coreyh.risktionary.game.socket.controller

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.messages.inbound.round.ChatMessageCommand
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameRoundChatSocketController(
    private val gameSessionService: GameSessionService,
) {
    @MessageMapping("/game/chat")
    fun onChatMessage(
        principal: GameSocketPrincipal,
        command: ChatMessageCommand,
    ) {
        if (principal !is GameSocketPrincipal.Player) return

        gameSessionService.handleChat(
            gameId = principal.gameId,
            playerId = principal.id,
            text = command.text,
        )
    }
}
