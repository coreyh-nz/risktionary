package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import kotlin.time.Instant

sealed class GameRoundPhase {
    abstract val type: RoundPhaseType
    abstract val endingAt: Instant?

    data object Initialising : GameRoundPhase() {
        override val type = RoundPhaseType.INITIALISING
        override val endingAt: Instant? = null
    }

    data class Drawing(
        override val endingAt: Instant?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.DRAWING
    }

    data class DrawingReview(
        override val endingAt: Instant?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.DRAWING_REVIEW
    }

    data class Ranking(
        override val endingAt: Instant?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.RANKING
    }

    data class RankingReview(
        override val endingAt: Instant?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.RANKING_REVIEW
    }

    data class WordReview(
        override val endingAt: Instant?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.WORD_REVIEW
    }

    data class Scoring(
        override val endingAt: Instant?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.SCORING
    }

    data class Completed(
        override val endingAt: Instant?,
    ) : GameRoundPhase() {
        override val type = RoundPhaseType.COMPLETED
    }
}
