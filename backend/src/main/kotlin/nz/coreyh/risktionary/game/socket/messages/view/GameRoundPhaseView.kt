package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import kotlin.time.Clock

sealed interface GameRoundPhaseView {
    val type: RoundPhaseType
    val timer: TimerView?

    data class Drawing(
        override val timer: TimerView?,
    ) : GameRoundPhaseView {
        override val type = RoundPhaseType.DRAWING
    }

    data class DrawingReview(
        override val timer: TimerView?,
        val word: String,
    ) : GameRoundPhaseView {
        override val type = RoundPhaseType.DRAWING_REVIEW
    }

    data class Ranking(
        override val timer: TimerView?,
    ) : GameRoundPhaseView {
        override val type = RoundPhaseType.RANKING
    }

    data class RankingReview(
        override val timer: TimerView?,
    ) : GameRoundPhaseView {
        override val type = RoundPhaseType.RANKING_REVIEW
    }

    data class WordReview(
        override val timer: TimerView?,
    ) : GameRoundPhaseView {
        override val type = RoundPhaseType.WORD_REVIEW
    }

    data object Saving : GameRoundPhaseView {
        override val type = RoundPhaseType.SAVING
        override val timer: TimerView? = null
    }

    data class Scoring(
        override val timer: TimerView?,
    ) : GameRoundPhaseView {
        override val type = RoundPhaseType.SCORING
    }

    data object Completed : GameRoundPhaseView {
        override val type = RoundPhaseType.COMPLETED
        override val timer: TimerView? = null
    }
}

fun GameRoundSession.toPhaseStateView(clock: Clock = Clock.System): GameRoundPhaseView {
    val state = requireState<GameRoundState.InProgress>()
    val timer = state.phase.timeWindow?.toTimerView(clock)
    return when (state.phase) {
        // client should never be sent this state
        is GameRoundPhase.Initialising -> {
            throw GameStateInvalidException()
        }

        is GameRoundPhase.Drawing -> {
            GameRoundPhaseView.Drawing(timer = timer)
        }

        is GameRoundPhase.DrawingReview -> {
            GameRoundPhaseView.DrawingReview(
                timer = timer,
                word = word.value,
            )
        }

        is GameRoundPhase.Ranking -> {
            GameRoundPhaseView.Ranking(timer = timer)
        }

        is GameRoundPhase.RankingReview -> {
            GameRoundPhaseView.RankingReview(timer = timer)
        }

        is GameRoundPhase.Saving -> {
            GameRoundPhaseView.Saving
        }

        is GameRoundPhase.Scoring -> {
            GameRoundPhaseView.Scoring(timer = timer)
        }

        is GameRoundPhase.WordReview -> {
            GameRoundPhaseView.WordReview(timer = timer)
        }

        is GameRoundPhase.Completed -> {
            GameRoundPhaseView.Completed
        }
    }
}
