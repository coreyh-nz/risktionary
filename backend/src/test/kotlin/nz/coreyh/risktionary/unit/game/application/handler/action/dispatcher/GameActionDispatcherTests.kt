package nz.coreyh.risktionary.unit.game.application.handler.action.dispatcher

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.verify
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.handler.action.GameActionHandler
import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameActionDispatcher
import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameRoundActionDispatcher
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.extensions.mockkRelaxed
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockKTest
class GameActionDispatcherTests {
    private lateinit var gameSessionStore: GameSessionStore
    private lateinit var gameRoundActionDispatcher: GameRoundActionDispatcher
    private val gameId = createTestGameId()
    private val playerId = createTestGamePlayerId()

    @BeforeEach
    fun setup() {
        gameSessionStore = mockkRelaxed()
        gameRoundActionDispatcher = mockkRelaxed()
    }

    @Test
    fun `dispatch dispatches action to matching session handler`() {
        val action = GameSessionTestAction()
        val session = mockkRelaxed<GameSession>()
        val player = mockkRelaxed<GamePlayerSession>()
        val handler = mockkRelaxed<GameActionHandler<GameSessionTestAction>>()
        every { gameSessionStore.findById(gameId) } returns session
        every { session.findPlayer(playerId) } returns player
        every { handler.actionClass } returns GameSessionTestAction::class

        val gameActionDispatcher = gameActionDispatcher(handler)
        gameActionDispatcher.dispatch(gameId, playerId, action)

        verify { handler.handle(session, player, action) }
        verify(exactly = 0) {
            gameRoundActionDispatcher.dispatch(any(), any(), any())
        }
    }

    @Test
    fun `dispatch dispatches action to round action dispatcher when no session handler matches`() {
        val action = GameSessionTestAction()
        val session = mockkRelaxed<GameSession>()
        val player = mockkRelaxed<GamePlayerSession>()
        val round = mockkRelaxed<GameRoundSession>()
        every { gameSessionStore.findById(gameId) } returns session
        every { session.findPlayer(playerId) } returns player
        every { session.currentRound } returns round

        val gameActionDispatcher = gameActionDispatcher()
        gameActionDispatcher.dispatch(gameId, playerId, action)

        verify {
            gameRoundActionDispatcher.dispatch(round, player, action)
        }
    }

    @Test
    fun `dispatch throws when game does not exist`() {
        val action = GameSessionTestAction()
        every { gameSessionStore.findById(gameId) } returns null

        val gameActionDispatcher = gameActionDispatcher()
        shouldThrow<GameNotFoundException> { gameActionDispatcher.dispatch(gameId, playerId, action) }

        verify(exactly = 0) {
            gameRoundActionDispatcher.dispatch(any(), any(), any())
        }
    }

    @Test
    fun `dispatch throws when player is not in session`() {
        val action = GameSessionTestAction()
        val session = mockkRelaxed<GameSession>()

        every { gameSessionStore.findById(gameId) } returns session
        every { session.findPlayer(playerId) } returns null

        val gameActionDispatcher = gameActionDispatcher()
        shouldThrow<GamePlayerNotInSessionException> { gameActionDispatcher.dispatch(gameId, playerId, action) }

        verify(exactly = 0) {
            gameRoundActionDispatcher.dispatch(any(), any(), any())
        }
    }

    @Test
    fun `dispatch throws when no session handler matches and no current round exists`() {
        val action = GameSessionTestAction()
        val session = mockkRelaxed<GameSession>()
        val player = mockkRelaxed<GamePlayerSession>()
        every { gameSessionStore.findById(gameId) } returns session
        every { session.findPlayer(playerId) } returns player
        every { session.currentRound } returns null

        val gameActionDispatcher = gameActionDispatcher()
        shouldThrow<GameRoundStateInvalidException> {
            gameActionDispatcher.dispatch(gameId, playerId, action)
        }

        verify(exactly = 0) {
            gameRoundActionDispatcher.dispatch(any(), any(), any())
        }
    }

    private fun gameActionDispatcher(vararg handlers: GameActionHandler<*>) =
        GameActionDispatcher(
            gameSessionStore = gameSessionStore,
            gameRoundActionDispatcher = gameRoundActionDispatcher,
            sessionHandlers = handlers.toList(),
        )

    private class GameSessionTestAction
}
