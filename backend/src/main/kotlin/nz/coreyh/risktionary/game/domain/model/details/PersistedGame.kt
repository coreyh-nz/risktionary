package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.domain.model.WordId
import kotlin.time.Duration
import kotlin.time.Instant

data class PersistedGame(
    val id: GameId,
    val code: String,
    val hostUserId: UserId,
    val createdAt: Instant,
    val feedbackGenerationMode: FeedbackGenerationMode,
    val lobbyCountdown: Duration,
    val skippingCountdownsEnabled: Boolean,
    val scoring: PersistedScoring,
    val phaseDurations: Map<RoundPhaseType, Duration>,
    val words: List<PersistedGameWord>,
    val endReason: GameEndReason?,
    val endedAt: Instant?,
)

data class PersistedScoring(
    val maxPoints: Int,
    val minPoints: Int,
    val untimedReferenceWindow: Duration,
)

data class PersistedGameWord(
    val position: Int,
    val wordId: WordId?,
    val wordValue: String,
)
