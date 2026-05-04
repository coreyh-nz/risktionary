package nz.coreyh.risktionary.game.socket.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.http.server.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

private val kLogger = KotlinLogging.logger {}

/**
 * Custom handshake handler that extracts and assigns the [GameSocketPrincipal] to WebSocket sessions.
 *
 * This handler works in conjunction with [WebSocketHandshakeInterceptor] which places the
 * principal into the handshake attributes map. During the STOMP handshake phase,
 * this handler retrieves that principal and assigns it to the WebSocket session's user property.
 *
 * Without this handler, the [GameSocketPrincipal] stored in attributes would not be propagated
 * to the STOMP session, making it unavailable for authentication and authorization checks
 * in interceptors like [WebSocketChannelInterceptor].
 */
@Component
class WebSocketHandshakeHandler : DefaultHandshakeHandler() {
    /**
     * Determines the [Principal] to associate with a WebSocket session during handshake.
     *
     * Retrieves the [GameSocketPrincipal] that was previously stored in the handshake attributes
     * by [WebSocketHandshakeInterceptor]. This principal is then returned and becomes available
     * via [org.springframework.messaging.simp.stomp.StompHeaderAccessor.user] in subsequent STOMP
     * message interceptors.
     *
     * If no principal is found in the attributes, returns `null` which will result in an
     * unauthenticated WebSocket session. Such sessions will be rejected when they attempt
     * to subscribe to protected destinations.
     *
     * @param request The HTTP request that initiated the WebSocket handshake
     * @param wsHandler The WebSocket handler that will process messages
     * @param attributes The handshake attributes map (populated by handshake interceptors)
     * @return The extracted [GameSocketPrincipal], or `null` if not found
     */
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
