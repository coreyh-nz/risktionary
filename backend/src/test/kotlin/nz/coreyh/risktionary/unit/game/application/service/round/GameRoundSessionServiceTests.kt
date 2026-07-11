package nz.coreyh.risktionary.unit.game.application.service.round

import io.mockk.mockk
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.support.annotation.MockKTest
import org.junit.jupiter.api.BeforeEach

@MockKTest
class GameRoundSessionServiceTests {
    private lateinit var gameSessionStore: GameSessionStore
    private lateinit var gameEventPublisher: GameEventPublisher
    private lateinit var service: GameRoundSessionService

    @BeforeEach
    fun setup() {
        gameSessionStore = mockk(relaxed = true)
        gameEventPublisher = mockk(relaxed = true)
        service = GameRoundSessionService(gameSessionStore, gameEventPublisher)
    }
}
