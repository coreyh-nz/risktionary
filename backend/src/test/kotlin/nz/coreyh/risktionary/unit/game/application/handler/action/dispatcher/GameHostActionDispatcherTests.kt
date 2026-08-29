package nz.coreyh.risktionary.unit.game.application.handler.action.dispatcher

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.verify
import nz.coreyh.risktionary.game.application.exception.GameActionNotSupportedException
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.handler.action.GameHostActionHandler
import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameHostActionDispatcher
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.extensions.mockkRelaxed
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockKTest
class GameHostActionDispatcherTests {
    private lateinit var gameSessionStore: GameSessionStore
    private val gameId = createTestGameId()
    private val hostId = createTestUserId()

    @BeforeEach
    fun setup() {
        gameSessionStore = mockkRelaxed()
    }

    @Test
    fun `dispatch dispatches action to matching host action handler`() {
        val action = GameSessionTestHostAction()
        val session = mockkRelaxed<GameSession>()
        val handler = mockkRelaxed<GameHostActionHandler<GameSessionTestHostAction>>()
        every { gameSessionStore.findById(gameId) } returns session
        every { session.host.id } returns hostId
        every { handler.actionClass } returns GameSessionTestHostAction::class

        val dispatcher = gameHostActionDispatcher(handler)
        dispatcher.dispatch(gameId, hostId, action)

        verify { handler.handle(session, action) }
    }

    @Test
    fun `dispatch throws game not found exception when game does not exist`() {
        val action = GameSessionTestHostAction()
        every { gameSessionStore.findById(gameId) } returns null

        val dispatcher = gameHostActionDispatcher()
        shouldThrow<GameNotFoundException> { dispatcher.dispatch(gameId, hostId, action) }
    }

    @Test
    fun `dispatch throws game not found exception when host does not match`() {
        val otherHostId = createTestUserId()
        val action = GameSessionTestHostAction()
        val session = mockkRelaxed<GameSession>()
        every { gameSessionStore.findById(gameId) } returns session
        every { session.host.id } returns otherHostId

        val dispatcher = gameHostActionDispatcher()
        shouldThrow<GameNotFoundException> { dispatcher.dispatch(gameId, hostId, action) }
    }

    @Test
    fun `dispatch throws game action not supported exception when action has no matching handler`() {
        val action = GameSessionTestHostAction()
        val session = mockkRelaxed<GameSession>()
        every { gameSessionStore.findById(gameId) } returns session
        every { session.host.id } returns hostId

        val dispatcher = gameHostActionDispatcher()
        shouldThrow<GameActionNotSupportedException> {
            dispatcher.dispatch(gameId, hostId, action)
        }
    }

    private fun gameHostActionDispatcher(vararg handlers: GameHostActionHandler<*>) =
        GameHostActionDispatcher(
            gameSessionStore = gameSessionStore,
            handlers = handlers.toList(),
        )

    private class GameSessionTestHostAction
}
