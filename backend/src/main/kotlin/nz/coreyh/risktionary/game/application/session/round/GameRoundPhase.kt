package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType

sealed interface GameRoundPhase {
    val type: RoundPhaseType

    data object Drawing : GameRoundPhase {
        override val type = RoundPhaseType.DRAWING
    }

    data object DrawingReview : GameRoundPhase {
        override val type: RoundPhaseType = RoundPhaseType.DRAWING_REVIEW
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

    data object Scoring : GameRoundPhase {
        override val type = RoundPhaseType.SCORING
    }

    data object Completed : GameRoundPhase {
        override val type = RoundPhaseType.COMPLETED
    }
}
