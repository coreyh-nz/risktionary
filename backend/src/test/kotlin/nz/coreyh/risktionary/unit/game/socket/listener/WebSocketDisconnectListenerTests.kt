package nz.coreyh.risktionary.unit.game.socket.listener

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.socket.listener.WebSocketDisconnectListener
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketHostPrincipal
import nz.coreyh.risktionary.support.factory.game.socket.createTestGameSocketPlayerPrincipal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import java.security.Principal

class WebSocketDisconnectListenerTests {
    private lateinit var gameSessionService: GameSessionService
    private lateinit var webSocketDisconnectListener: WebSocketDisconnectListener

    @BeforeEach
    fun setup() {
        gameSessionService = mockk(relaxed = true)
        webSocketDisconnectListener = WebSocketDisconnectListener(gameSessionService)
    }

    @Test
    fun `on application event calls handle connected when principal is player`() {
        val principal = createTestGameSocketPlayerPrincipal()
        val event = mockk<SessionDisconnectEvent> { every { user } returns principal }

        webSocketDisconnectListener.onApplicationEvent(event)

        verify(exactly = 1) {
            gameSessionService.handleDisconnected(
                gameId = principal.gameId,
                playerId = principal.id,
            )
        }
        verify(exactly = 0) { gameSessionService.handleHostDisconnected(any(), any()) }
    }

    @Test
    fun `on application event calls handle host connected when principal is host`() {
        val principal = createTestGameSocketHostPrincipal()
        val event = mockk<SessionDisconnectEvent> { every { user } returns principal }

        webSocketDisconnectListener.onApplicationEvent(event)

        verify(exactly = 1) {
            gameSessionService.handleHostDisconnected(
                gameId = principal.gameId,
                hostId = principal.id,
            )
        }
        verify(exactly = 0) { gameSessionService.handleDisconnected(any(), any()) }
    }

    @Test
    fun `on application event does nothing when principal is null`() {
        val event = mockk<SessionDisconnectEvent> { every { user } returns null }

        webSocketDisconnectListener.onApplicationEvent(event)

        verify(exactly = 0) { gameSessionService.handleDisconnected(any(), any()) }
        verify(exactly = 0) { gameSessionService.handleHostDisconnected(any(), any()) }
    }

    @Test
    fun `on application event does nothing when principal is not a game socket principal`() {
        val principal = mockk<Principal>()
        val event = mockk<SessionDisconnectEvent> { every { user } returns principal }

        webSocketDisconnectListener.onApplicationEvent(event)

        verify(exactly = 0) { gameSessionService.handleDisconnected(any(), any()) }
        verify(exactly = 0) { gameSessionService.handleHostDisconnected(any(), any()) }
    }
}
