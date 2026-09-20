package nz.coreyh.risktionary.game.application.handler.state.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.application.handler.state.GameRoundPhaseStateHandler
import nz.coreyh.risktionary.game.application.service.GameResearchPersistenceService
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.config.GameResearchPersistenceProperties
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Handles entry for the [GameRoundPhase.Saving] phase, which writes the
 * round's research data to the database.
 *
 * Feedback generation can still be running when the round gets here, so this
 * first waits for it, up to the configured timeout. Calls still running after
 * that are abandoned, logged at ERROR, and counted on the persisted round so
 * incomplete rounds can be identified.
 *
 * The write is synchronous and any failure propagates: the round stays in
 * the saving phase rather than moving on with unrecorded data.
 */
@Service
class GameRoundPhaseStateSavingHandler(
    private val gameResearchPersistenceService: GameResearchPersistenceService,
    private val gameResearchPersistenceProperties: GameResearchPersistenceProperties,
) : GameRoundPhaseStateHandler<GameRoundPhase.Saving> {
    override val phaseClass = GameRoundPhase.Saving::class

    override fun onEnter(
        round: GameRoundSession,
        state: GameRoundState.InProgress,
        phase: GameRoundPhase.Saving,
    ) {
        val abandoned = round.feedback.awaitPending(gameResearchPersistenceProperties.pendingAiTimeout)
        if (abandoned > 0) {
            logger.error {
                "Abandoned $abandoned AI call(s) still running after " +
                    "${gameResearchPersistenceProperties.pendingAiTimeout} (round=${round.id}); saving the round without them"
            }
        }

        gameResearchPersistenceService.persistRound(round, abandonedAiCalls = abandoned)
    }
}
