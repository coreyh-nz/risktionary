package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.domain.model.TimeWindow
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType

sealed class GameRoundPhase {
    abstract val type: RoundPhaseType
    abstract val timeWindow: TimeWindow?

    data object Initialising : GameRoundPhase() {
        override val type = RoundPhaseType.INITIALISING
        override val timeWindow: TimeWindow? = null
    }

    data class Drawing(
        override val timeWindow: TimeWindow?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.DRAWING
    }

    data class DrawingReview(
        override val timeWindow: TimeWindow?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.DRAWING_REVIEW
    }

    data class Ranking(
        override val timeWindow: TimeWindow?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.RANKING
    }

    data class RankingReview(
        override val timeWindow: TimeWindow?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.RANKING_REVIEW
    }

    data class WordReview(
        override val timeWindow: TimeWindow?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.WORD_REVIEW
    }

    data object Saving : GameRoundPhase() {
        override val type = RoundPhaseType.SAVING
        override val timeWindow: TimeWindow? = null
    }

    data class Scoring(
        override val timeWindow: TimeWindow?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.SCORING
    }

    data object Completed : GameRoundPhase() {
        override val type = RoundPhaseType.COMPLETED
        override val timeWindow = null
    }
}
