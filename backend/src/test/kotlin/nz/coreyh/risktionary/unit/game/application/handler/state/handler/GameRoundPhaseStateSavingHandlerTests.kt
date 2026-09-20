package nz.coreyh.risktionary.unit.game.application.handler.state.handler

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import nz.coreyh.risktionary.game.application.handler.state.handler.GameRoundPhaseStateSavingHandler
import nz.coreyh.risktionary.game.application.service.GameResearchPersistenceService
import nz.coreyh.risktionary.game.application.session.round.GameRoundFeedbackSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.config.GameResearchPersistenceProperties
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayer
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class GameRoundPhaseStateSavingHandlerTests {
    private val persistenceService = mockk<GameResearchPersistenceService>()
    private val feedbackSession = mockk<GameRoundFeedbackSession>()
    private val round =
        mockk<GameRoundSession> {
            every { feedback } returns feedbackSession
        }
    private val handler =
        GameRoundPhaseStateSavingHandler(persistenceService, GameResearchPersistenceProperties(Duration.ofSeconds(10)))

    private fun enter() =
        handler.onEnter(round, GameRoundState.InProgress(GameRoundPhase.Saving, createTestGamePlayer()), GameRoundPhase.Saving)

    @Test
    fun `entering waits for pending feedback before persisting the round`() {
        every { feedbackSession.awaitPending(any()) } returns 0
        every { persistenceService.persistRound(round, 0) } just Runs

        enter()

        verifyOrder {
            feedbackSession.awaitPending(10.seconds)
            persistenceService.persistRound(round, 0)
        }
    }

    @Test
    fun `entering persists the round with the number of abandoned calls when the wait times out`() {
        every { feedbackSession.awaitPending(any()) } returns 2
        every { persistenceService.persistRound(round, 2) } just Runs

        enter()

        verify { persistenceService.persistRound(round, 2) }
    }
}
