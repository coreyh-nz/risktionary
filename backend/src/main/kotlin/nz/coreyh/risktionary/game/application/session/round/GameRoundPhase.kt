package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType

sealed interface GameRoundPhase {
    val type: RoundPhaseType

    data object Drawing : GameRoundPhase {
        override val type = RoundPhaseType.DRAWING
    }

    data class DrawingReview(
        val word: String,
    ) : GameRoundPhase {
        override val type = RoundPhaseType.DRAWING_REVIEW
    }

    data object Ranking : GameRoundPhase {
        override val type = RoundPhaseType.RANKING
    }

    data object RankingReview : GameRoundPhase {
        override val type = RoundPhaseType.RANKING_REVIEW
    }

    data object WordReview : GameRoundPhase {
        override val type = RoundPhaseType.WORD_REVIEW
    }

    data object RoundScoring : GameRoundPhase {
        override val type = RoundPhaseType.ROUND_SCORING
    }

    data object Completed : GameRoundPhase {
        override val type = RoundPhaseType.COMPLETED
    }
}
