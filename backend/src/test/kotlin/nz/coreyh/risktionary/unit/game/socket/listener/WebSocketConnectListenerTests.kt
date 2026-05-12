package nz.coreyh.risktionary.unit.game.socket.listener

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.listener.WebSocketConnectListener
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketHostPrincipal
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketPlayerPrincipal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.socket.messaging.SessionConnectEvent
import java.security.Principal

class WebSocketConnectListenerTests {
    private lateinit var gameSessionService: GameSessionService
    private lateinit var webSocketConnectListener: WebSocketConnectListener

    @BeforeEach
    fun setup() {
        gameSessionService = mockk()
        webSocketConnectListener = WebSocketConnectListener(gameSessionService)
    }

    @Test
    fun `on application event calls handle connecting when principal is player`() {
        val principal = createTestGameSocketPlayerPrincipal()
        val event = mockk<SessionConnectEvent> { every { user } returns principal }
        every {
            gameSessionService.handleConnecting(gameId = principal.gameId, playerId = principal.id)
        } just Runs

        webSocketConnectListener.onApplicationEvent(event)

        verify(exactly = 1) {
            gameSessionService.handleConnecting(gameId = principal.gameId, playerId = principal.id)
        }
    }

    @Test
    fun `on application event does nothing when principal is host`() {
        val principal = createTestGameSocketHostPrincipal()
        val event = mockk<SessionConnectEvent> { every { user } returns principal }

        webSocketConnectListener.onApplicationEvent(event)
    }

    @Test
    fun `on application event does nothing when principal is null`() {
        val event = mockk<SessionConnectEvent> { every { user } returns null }

        webSocketConnectListener.onApplicationEvent(event)
    }

    @Test
    fun `on application event does nothing when principal is not a game socket principal`() {
        val principal = mockk<Principal>()
        val event = mockk<SessionConnectEvent> { every { user } returns principal }

        webSocketConnectListener.onApplicationEvent(event)
    }
}
