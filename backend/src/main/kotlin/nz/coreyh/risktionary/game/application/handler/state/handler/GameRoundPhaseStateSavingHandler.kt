package nz.coreyh.risktionary.game.application.handler.state.handler

import nz.coreyh.risktionary.game.application.handler.state.GameRoundPhaseStateHandler
import nz.coreyh.risktionary.game.application.service.GameResearchPersistenceService
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import org.springframework.stereotype.Service

/**
 * Handles entry for the [GameRoundPhase.Saving] phase, which writes the
 * round's research data to the database.
 *
 * The write is synchronous and any failure propagates: the round stays in
 * the saving phase rather than moving on with unrecorded data.
 */
@Service
class GameRoundPhaseStateSavingHandler(
    private val gameResearchPersistenceService: GameResearchPersistenceService,
) : GameRoundPhaseStateHandler<GameRoundPhase.Saving> {
    override val phaseClass = GameRoundPhase.Saving::class

    override fun onEnter(
        round: GameRoundSession,
        state: GameRoundState.InProgress,
        phase: GameRoundPhase.Saving,
    ) {
        gameResearchPersistenceService.persistRound(round)
    }
}
