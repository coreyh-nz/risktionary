package nz.coreyh.risktionary.game.socket.listener

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketDisconnectListener(
    private val gameSessionService: GameSessionService,
) : ApplicationListener<SessionDisconnectEvent> {
    override fun onApplicationEvent(event: SessionDisconnectEvent) {
        val principal = event.user as? GameSocketPrincipal ?: return
        when (principal) {
            is GameSocketPrincipal.Player -> {
                gameSessionService.handleDisconnected(
                    gameId = principal.gameId,
                    playerId = principal.id,
                )
            }

            is GameSocketPrincipal.Host -> {
                gameSessionService.handleHostDisconnected(
                    gameId = principal.gameId,
                    hostId = principal.id,
                )
            }
        }
    }
}
