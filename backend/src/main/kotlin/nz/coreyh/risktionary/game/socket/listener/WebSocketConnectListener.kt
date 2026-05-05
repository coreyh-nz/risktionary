package nz.coreyh.risktionary.game.socket.listener

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent

@Component
class WebSocketConnectListener(
    private val gameSessionService: GameSessionService,
) : ApplicationListener<SessionConnectEvent> {
    override fun onApplicationEvent(event: SessionConnectEvent) {
        val principal = event.user as? GameSocketPrincipal ?: return
        if (principal !is GameSocketPrincipal.Player) return
        gameSessionService.handleConnecting(gameId = principal.gameId, playerId = principal.id)
    }
}
