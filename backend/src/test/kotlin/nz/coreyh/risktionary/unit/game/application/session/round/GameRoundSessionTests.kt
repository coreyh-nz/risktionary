package nz.coreyh.risktionary.unit.game.application.session.round

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.domain.model.createGameId
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.word.createTestWord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameRoundSessionTests {
    private lateinit var session: GameRoundSession

    @BeforeEach
    fun setup() {
        session =
            GameRoundSession(
                id = createRoundId(),
                gameId = createGameId(),
                word = createTestWord(),
            )
    }

    @Test
    fun `initial state is SelectingDrawer`() {
        session.state.shouldBeInstanceOf<GameRoundState.SelectingDrawer>()
    }

    @Test
    fun `select drawer transitions state to in progress when in selecting drawer state`() {
        val drawerId = createTestGamePlayerId()

        session.selectDrawer(drawerId)

        session.state.shouldBeInstanceOf<GameRoundState.InProgress>()
    }

    @Test
    fun `select drawer sets correct drawerId when transitioning to in progress`() {
        val drawerId = createTestGamePlayerId()

        session.selectDrawer(drawerId)

        val state = session.state as GameRoundState.InProgress
        state.drawerId shouldBe drawerId
    }

    @Test
    fun `select drawer throws when round is already in progress`() {
        val drawerId = createTestGamePlayerId()
        session.selectDrawer(drawerId)

        shouldThrow<GameRoundStateInvalidException> {
            session.selectDrawer(createTestGamePlayerId())
        }
    }
}
