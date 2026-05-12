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
        when (principal) {
            is GameSocketPrincipal.Player -> {
                gameSessionService.handleConnected(
                    gameId = principal.gameId,
                    playerId = principal.id,
                )
            }

            is GameSocketPrincipal.Host -> {
                gameSessionService.handleHostConnected(
                    gameId = principal.gameId,
                    hostId = principal.id,
                )
            }
        }
    }
}
