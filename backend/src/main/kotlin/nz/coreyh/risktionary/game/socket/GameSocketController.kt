package nz.coreyh.risktionary.game.socket

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameSocketController(
    private val gameSessionService: GameSessionService,
) {
    @MessageMapping("/player/ready")
    fun onReady(principal: GameSocketPrincipal) {
        if (principal !is GameSocketPrincipal.Player) return

        val playerId = principal.id
        gameSessionService.handleReady(playerId)
    }

    @MessageMapping("/game/start")
    fun startGame(principal: GameSocketPrincipal) {
        if (principal !is GameSocketPrincipal.Host) return
        val gameId = principal.gameId
        gameSessionService.transitionToStarting(gameId)
    }
}
