package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType

sealed interface GameRoundPhaseView {
    val type: RoundPhaseType

    data object Drawing : GameRoundPhaseView {
        override val type = RoundPhaseType.DRAWING
    }

    data class DrawingReview(
        val word: String,
    ) : GameRoundPhaseView {
        override val type = RoundPhaseType.DRAWING_REVIEW
    }

    data object Ranking : GameRoundPhaseView {
        override val type = RoundPhaseType.RANKING
    }

    data object RankingReview : GameRoundPhaseView {
        override val type = RoundPhaseType.RANKING_REVIEW
    }

    data object WordReview : GameRoundPhaseView {
        override val type = RoundPhaseType.WORD_REVIEW
    }

    data object Scoring : GameRoundPhaseView {
        override val type = RoundPhaseType.SCORING
    }

    data object Completed : GameRoundPhaseView {
        override val type = RoundPhaseType.COMPLETED
    }
}

fun GameRoundSession.toPhaseStateView(): GameRoundPhaseView {
    val state = requireState<GameRoundState.InProgress>()
    return when (state.phase) {
        // client should never be sent this state
        is GameRoundPhase.Initialising -> throw GameStateInvalidException()

        is GameRoundPhase.Drawing -> GameRoundPhaseView.Drawing

        is GameRoundPhase.DrawingReview -> GameRoundPhaseView.DrawingReview(word.value)

        is GameRoundPhase.Ranking -> GameRoundPhaseView.Ranking

        is GameRoundPhase.RankingReview -> GameRoundPhaseView.RankingReview

        is GameRoundPhase.Scoring -> GameRoundPhaseView.Scoring

        is GameRoundPhase.WordReview -> GameRoundPhaseView.WordReview

        is GameRoundPhase.Completed -> GameRoundPhaseView.Completed
    }
}
