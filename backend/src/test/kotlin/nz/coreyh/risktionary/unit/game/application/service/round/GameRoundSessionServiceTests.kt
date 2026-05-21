package nz.coreyh.risktionary.unit.game.application.service.round

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.word.createTestWord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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

    @Nested
    @MockKTest
    inner class CreateRound {
        private val gameId = createTestGameId()
        private val word = createTestWord()

        @Test
        fun `create round sets current round on session`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session

            service.createRound(gameId, word)

            verify { session.currentRound = any() }
        }

        @Test
        fun `create round sets round with correct word`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session

            service.createRound(gameId, word)

            verify { session.currentRound = match { it.word == word } }
        }

        @Test
        fun `create round publishes round state changed event`() {
            val session = mockk<GameSession>(relaxed = true)
            every { gameSessionStore.findById(gameId) } returns session

            service.createRound(gameId, word)

            verify { gameEventPublisher.publishRoundStateChanged(gameId, GameRoundState.SelectingDrawer) }
        }

        @Test
        fun `create round throws when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            shouldThrow<GameNotFoundException> {
                service.createRound(gameId, word)
            }
        }

        @Test
        fun `create round does not publish event when session does not exist`() {
            every { gameSessionStore.findById(gameId) } returns null

            runCatching { service.createRound(gameId, word) }

            verify(exactly = 0) { gameEventPublisher.publishRoundStateChanged(any(), any()) }
        }
    }
}
