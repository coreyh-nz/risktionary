package nz.coreyh.risktionary.game.config

import nz.coreyh.risktionary.game.socket.interceptor.WebSocketChannelInterceptor
import nz.coreyh.risktionary.game.socket.interceptor.WebSocketHandshakeHandler
import nz.coreyh.risktionary.game.socket.interceptor.WebSocketHandshakeInterceptor
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

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfiguration(
    private val webSocketHandshakeInterceptor: WebSocketHandshakeInterceptor,
    private val webSocketChannelInterceptor: WebSocketChannelInterceptor,
    private val webSocketHandshakeHandler: WebSocketHandshakeHandler,
    private val appProperties: AppProperties,
) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // destinations prefixed with Topic.PREFIX are broadcast to all subscribers
        // destinations prefixed with USER_PREFIX are routed to a specific user
        registry.enableSimpleBroker(
            Topic.PREFIX,
            Queue.PREFIX,
        )

        // destinations prefixed with App.PREFIX are routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes(App.PREFIX)

        // required for WebSocketDestinations.USER_PREFIX destinations to work
        registry.setUserDestinationPrefix(WebSocketDestinations.USER_PREFIX)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws")
            .setHandshakeHandler(webSocketHandshakeHandler)
            .addInterceptors(webSocketHandshakeInterceptor)
            .setAllowedOriginPatterns(appProperties.frontendUrl)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(webSocketChannelInterceptor)
    }
}
