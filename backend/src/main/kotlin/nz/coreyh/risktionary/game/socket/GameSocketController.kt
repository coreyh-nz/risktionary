package nz.coreyh.risktionary.game.socket

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingCanvasClearCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingSnapshotCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingStrokeEndCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingStrokePointsCommand
import nz.coreyh.risktionary.game.socket.messages.inbound.drawing.DrawingStrokeStartCommand
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingCanvasClearEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingStrokeEndEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingStrokePointsEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingStrokeStartEvent
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class GameSocketController(
    private val gameSessionService: GameSessionService,
    private val simpMessagingTemplate: SimpMessagingTemplate,
) {
    @MessageMapping("/player/ready")
    fun onReady(principal: GameSocketPrincipal) {
        when (principal) {
            is GameSocketPrincipal.Player -> gameSessionService.handleReady(principal.id)
            is GameSocketPrincipal.Host -> gameSessionService.handleHostReady(principal.id)
        }
    }

    @MessageMapping("/game/start")
    fun startGame(principal: GameSocketPrincipal) {
        if (principal !is GameSocketPrincipal.Host) return
        val gameId = principal.gameId
        gameSessionService.transitionToStarting(gameId)
    }

    @MessageMapping("/game/draw")
    fun onDraw(
        principal: GameSocketPrincipal,
        command: DrawingCommand,
    ) {
        val gameId = principal.gameId
        val id =
            when (principal) {
                is GameSocketPrincipal.Host -> principal.id
                is GameSocketPrincipal.Player -> principal.id
            }
        val event =
            when (command) {
                is DrawingStrokeStartCommand -> {
                    DrawingStrokeStartEvent(
                        drawerId = id,
                        tool = command.tool,
                        point = command.point,
                        colour = command.colour,
                    )
                }

                is DrawingStrokePointsCommand -> {
                    DrawingStrokePointsEvent(
                        drawerId = id,
                        points = command.points,
                    )
                }

                is DrawingStrokeEndCommand -> {
                    DrawingStrokeEndEvent(
                        drawerId = id,
                    )
                }

                is DrawingCanvasClearCommand -> {
                    DrawingCanvasClearEvent()
                }

                is DrawingSnapshotCommand -> {
                    // snapshot command is handled via /game/draw/snapshot and is not sent to others
                    return
                }
            }

        simpMessagingTemplate.convertAndSend(
            WebSocketDestinations.Topic.draw(gameId),
            event,
        )
    }
}
