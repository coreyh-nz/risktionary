package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.TimeWindow
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import org.springframework.stereotype.Service
import kotlin.time.Clock

/**
 * Determines what phase follows the current one for a given round.
 */
@Service
class GameRoundPhaseStateTransitionService(
    private val clock: Clock = Clock.System,
) {
    /**
     * Returns the phase that follows [current], or null if [current] is the
     * last phase of the round.
     */
    fun next(
        round: GameRoundSession,
        current: GameRoundPhase,
    ): GameRoundPhase {
        val config = round.game.config
        return when (current) {
            is GameRoundPhase.Initialising -> {
                GameRoundPhase.Drawing(
                    timeWindow = phaseTiming(RoundPhaseType.DRAWING, config, clock),
                )
            }

            is GameRoundPhase.Drawing -> {
                GameRoundPhase.DrawingReview(
                    timeWindow = phaseTiming(RoundPhaseType.DRAWING_REVIEW, config, clock),
                )
            }

            is GameRoundPhase.DrawingReview -> {
                GameRoundPhase.Ranking(
                    timeWindow = phaseTiming(RoundPhaseType.RANKING, config, clock),
                )
            }

            is GameRoundPhase.Ranking -> {
                GameRoundPhase.RankingReview(
                    timeWindow = phaseTiming(RoundPhaseType.RANKING_REVIEW, config, clock),
                )
            }

            is GameRoundPhase.RankingReview -> {
                GameRoundPhase.WordReview(
                    timeWindow = phaseTiming(RoundPhaseType.WORD_REVIEW, config, clock),
                )
            }

            is GameRoundPhase.WordReview -> {
                GameRoundPhase.Scoring(
                    timeWindow = phaseTiming(RoundPhaseType.SCORING, config, clock),
                )
            }

            is GameRoundPhase.Scoring -> {
                GameRoundPhase.Completed
            }

            is GameRoundPhase.Completed -> {
                throw GameStateInvalidException()
            }
        }
    }

    private fun phaseTiming(
        phase: RoundPhaseType,
        config: GameConfiguration,
        clock: Clock,
    ) = config.phaseDurations[phase]?.let {
        TimeWindow(
            startedAt = clock.now(),
            duration = it,
        )
    }
}
