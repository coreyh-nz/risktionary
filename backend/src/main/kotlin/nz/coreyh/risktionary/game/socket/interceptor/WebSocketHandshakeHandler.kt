package nz.coreyh.risktionary.game.socket.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.http.server.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

private val kLogger = KotlinLogging.logger {}

@Component
class WebSocketHandshakeHandler : DefaultHandshakeHandler() {
    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>,
    ): Principal? {
        val principal = attributes["principal"] as? GameSocketPrincipal
        if (principal == null) {
            kLogger.debug {
                "WebSocket handshake: no principal found in attributes, rejecting user assignment"
            }
            return null
        }

        kLogger.debug {
            "WebSocket handshake: principal assigned (type=${principal::class.simpleName}, user=${principal.name})"
        }
        return principal
    }
}
