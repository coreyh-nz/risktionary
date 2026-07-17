package nz.coreyh.risktionary.game.application.service.round.phase

import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import org.springframework.stereotype.Service
import kotlin.time.Clock

/**
 * Determines what phase follows the current one for a given round.
 */
@Service
class GameRoundPhaseTransitionService(
    private val clock: Clock = Clock.System,
) {
    /**
     * Returns the phase that follows [current], or null if [current] is the
     * last phase of the round.
     */
    fun next(
        round: GameRoundSession,
        current: GameRoundPhase,
    ): GameRoundPhase? {
        val config = round.game.config
        return when (current) {
            is GameRoundPhase.Initialising -> {
                GameRoundPhase.Drawing(
                    endingAt = config.endingAtTimeFor(RoundPhaseType.DRAWING),
                )
            }

            is GameRoundPhase.Drawing -> {
                GameRoundPhase.DrawingReview(
                    endingAt = config.endingAtTimeFor(RoundPhaseType.DRAWING_REVIEW),
                )
            }

            is GameRoundPhase.DrawingReview -> {
                GameRoundPhase.Ranking(
                    endingAt = config.endingAtTimeFor(RoundPhaseType.RANKING),
                )
            }

            is GameRoundPhase.Ranking -> {
                GameRoundPhase.RankingReview(
                    endingAt = config.endingAtTimeFor(RoundPhaseType.RANKING_REVIEW),
                )
            }

            is GameRoundPhase.RankingReview -> {
                GameRoundPhase.WordReview(
                    endingAt = config.endingAtTimeFor(RoundPhaseType.WORD_REVIEW),
                )
            }

            is GameRoundPhase.WordReview -> {
                GameRoundPhase.Scoring(
                    endingAt = config.endingAtTimeFor(RoundPhaseType.SCORING),
                )
            }

            is GameRoundPhase.Scoring -> {
                GameRoundPhase.Completed(
                    endingAt = config.endingAtTimeFor(RoundPhaseType.COMPLETED),
                )
            }

            is GameRoundPhase.Completed -> {
                null
            }
        }
    }

    private fun GameConfiguration.endingAtTimeFor(phase: RoundPhaseType) = phaseDurations[phase]?.let { clock.now() + it }
}
