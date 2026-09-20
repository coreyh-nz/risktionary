package nz.coreyh.risktionary.game.socket.controller

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingSnapshotCommand
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameRoundDrawingSocketController(
    private val gameSessionService: GameSessionService,
) {
    @MessageMapping("/game/draw/snapshot")
    fun onDrawingSnapshot(
        principal: GameSocketPrincipal,
        command: DrawingSnapshotCommand,
    ) {
        if (principal !is GameSocketPrincipal.Host) return

        gameSessionService.handleDrawingSnapshot(gameId = principal.gameId, dataUrl = command.data)
    }
}
