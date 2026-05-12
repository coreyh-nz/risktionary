package nz.coreyh.risktionary.unit.game.socket.listener

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.listener.WebSocketConnectedListener
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketHostPrincipal
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketPlayerPrincipal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.socket.messaging.SessionConnectedEvent
import java.security.Principal

class WebSocketConnectedListenerTests {
    private lateinit var gameSessionService: GameSessionService
    private lateinit var webSocketConnectedListener: WebSocketConnectedListener

    @BeforeEach
    fun setup() {
        gameSessionService = mockk(relaxed = true)
        webSocketConnectedListener = WebSocketConnectedListener(gameSessionService)
    }

    @Test
    fun `on application event calls handle connected when principal is player`() {
        val principal = createTestGameSocketPlayerPrincipal()
        val event = mockk<SessionConnectedEvent> { every { user } returns principal }

        webSocketConnectedListener.onApplicationEvent(event)

        verify(exactly = 1) { gameSessionService.handleConnected(gameId = principal.gameId, playerId = principal.id) }
        verify(exactly = 0) { gameSessionService.handleHostConnected(any(), any()) }
    }

    @Test
    fun `on application event calls handle host connected when principal is host`() {
        val principal = createTestGameSocketHostPrincipal()
        val event = mockk<SessionConnectedEvent> { every { user } returns principal }

        webSocketConnectedListener.onApplicationEvent(event)

        verify(exactly = 1) { gameSessionService.handleHostConnected(gameId = principal.gameId, hostId = principal.id) }
        verify(exactly = 0) { gameSessionService.handleConnected(any(), any()) }
    }

    @Test
    fun `on application event does nothing when principal is null`() {
        val event = mockk<SessionConnectedEvent> { every { user } returns null }

        webSocketConnectedListener.onApplicationEvent(event)

        verify(exactly = 0) { gameSessionService.handleConnected(any(), any()) }
        verify(exactly = 0) { gameSessionService.handleHostConnected(any(), any()) }
    }

    @Test
    fun `on application event does nothing when principal is not a game socket principal`() {
        val principal = mockk<Principal>()
        val event = mockk<SessionConnectedEvent> { every { user } returns principal }

        webSocketConnectedListener.onApplicationEvent(event)

        verify(exactly = 0) { gameSessionService.handleConnected(any(), any()) }
        verify(exactly = 0) { gameSessionService.handleHostConnected(any(), any()) }
    }
}
