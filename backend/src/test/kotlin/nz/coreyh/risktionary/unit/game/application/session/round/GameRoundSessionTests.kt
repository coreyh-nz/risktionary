package nz.coreyh.risktionary.unit.game.application.session.round

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.word.createTestWord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameRoundSessionTests {
    private lateinit var gameSession: GameSession
    private lateinit var roundSession: GameRoundSession

    @BeforeEach
    fun setup() {
        gameSession = mockk()
        roundSession =
            GameRoundSession(
                id = createRoundId(),
                game = gameSession,
                word = createTestWord(),
            )
    }

    @Test
    fun `initial state is SelectingDrawer`() {
        roundSession.state.shouldBeInstanceOf<GameRoundState.SelectingDrawer>()
    }

    @Test
    fun `select drawer transitions state to in progress when in selecting drawer state`() {
        val drawer = createTestGamePlayerSession()

        roundSession.selectDrawer(drawer)

        roundSession.state.shouldBeInstanceOf<GameRoundState.InProgress>()
    }

    @Test
    fun `select drawer sets correct drawerId when transitioning to in progress`() {
        val drawer = createTestGamePlayerSession()

        roundSession.selectDrawer(drawer)

        val state = roundSession.state as GameRoundState.InProgress
        state.drawer shouldBe drawer.player
    }

    @Test
    fun `select drawer throws when round is already in progress`() {
        val drawer = createTestGamePlayerSession()
        roundSession.selectDrawer(drawer)

        shouldThrow<GameRoundStateInvalidException> {
            roundSession.selectDrawer(createTestGamePlayerSession())
        }
    }
}
