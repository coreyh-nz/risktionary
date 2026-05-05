package nz.coreyh.risktionary.game.socket.listener

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent

@Component
class WebSocketConnectedListener(
    private val gameSessionService: GameSessionService,
) : ApplicationListener<SessionConnectedEvent> {
    override fun onApplicationEvent(event: SessionConnectedEvent) {
        val principal = event.user as? GameSocketPrincipal ?: return
        if (principal !is GameSocketPrincipal.Player) return
        gameSessionService.handleConnected(gameId = principal.gameId, playerId = principal.id)
    }
}
