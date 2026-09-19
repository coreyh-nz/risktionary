package nz.coreyh.risktionary.game.config

import nz.coreyh.risktionary.game.socket.interceptor.WebSocketChannelInterceptor
import nz.coreyh.risktionary.game.socket.interceptor.WebSocketHandshakeHandler
import nz.coreyh.risktionary.game.socket.interceptor.WebSocketHandshakeInterceptor
import nz.coreyh.risktionary.game.socket.interceptor.WebSocketLoggingDirection
import nz.coreyh.risktionary.game.socket.interceptor.WebSocketLoggingInterceptor
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations.App
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations.Queue
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations.Topic
import nz.coreyh.risktionary.shared.config.AppProperties
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration

/**
 * WebSocket configuration that establishes the STOMP message broker infrastructure for real-time game communication.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfiguration(
    private val webSocketHandshakeInterceptor: WebSocketHandshakeInterceptor,
    private val webSocketChannelInterceptor: WebSocketChannelInterceptor,
    private val webSocketHandshakeHandler: WebSocketHandshakeHandler,
    private val appProperties: AppProperties,
) : WebSocketMessageBrokerConfigurer {
    /**
     * Configures the message broker and application destination prefixes.
     *
     * Enables a simple in-memory broker that handles:
     * - Topic destinations (`/topic/...`): Broadcast messages to all subscribers of that topic
     * - Queue destinations (`/queue/...`): Point-to-point messaging for user-specific messages
     *
     * Sets up application destination prefixes for client-to-server messages:
     * - Messages sent to `/app/...` are routed to `@MessageMapping` annotated controller methods
     *
     * Configures user destination prefix to support user-specific routing:
     * - Clients subscribe to `/user/queue/...` which gets translated to `/queue/...-user{sessionId}`
     * - Enables private messages and user-specific notifications
     */
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker(
            Topic.PREFIX,
            Queue.PREFIX,
        )
        registry.setApplicationDestinationPrefixes(App.PREFIX)
        registry.setUserDestinationPrefix(WebSocketDestinations.USER_PREFIX)
        registry.configureBrokerChannel().interceptors(WebSocketLoggingInterceptor(WebSocketLoggingDirection.OUTBOUND))
    }

    /**
     * Registers the STOMP endpoint that clients connect to for WebSocket communication.
     *
     * Configures the endpoint at `/ws` with:
     * - Custom handshake handler ([WebSocketHandshakeHandler]) that assigns the [nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal]
     *   to the WebSocket session after successful authentication
     * - Handshake interceptor ([WebSocketHandshakeInterceptor]) that validates player tickets
     *   or host authentication before the WebSocket connection is established
     * - Allowed origin patterns from application configuration to enforce CORS policies
     *
     * @param registry The STOMP endpoint registry to configure
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws")
            .setHandshakeHandler(webSocketHandshakeHandler)
            .addInterceptors(webSocketHandshakeInterceptor)
            .setAllowedOriginPatterns(appProperties.frontendUrl)
    }

    /**
     * Adds channel interceptors to the client inbound channel for message validation.
     *
     * Registers [WebSocketChannelInterceptor] which intercepts all messages sent from clients
     * to the server before they reach the message broker or controller methods.
     *
     * @param registration The channel registration to add interceptors to
     */
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(
            webSocketChannelInterceptor,
            WebSocketLoggingInterceptor(WebSocketLoggingDirection.INBOUND),
        )
    }

    override fun configureWebSocketTransport(registration: WebSocketTransportRegistration) {
        registration
            .setMessageSizeLimit(512 * 1024) // 512KB,
            .setSendBufferSizeLimit(1024 * 1024) // 1MB
            .setSendTimeLimit(20 * 1000)
    }
}
