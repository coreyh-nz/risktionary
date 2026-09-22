package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.GameRoundAiUsage
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess
import java.util.UUID
import kotlin.time.Instant

/** Everything persisted about one game, for research analysis. */
data class PersistedGameDetails(
    val game: PersistedGame,
    val players: List<PersistedGamePlayer>,
    val rounds: List<PersistedRoundDetails>,
    val aiUsageSummary: AiUsageSummary,
)

/**
 * Token usage across every round of a game, totalled by model.
 *
 * There is no AI provider concept on this branch yet - each model name stands
 * in for whichever provider served it. Once providers exist this should be
 * keyed by provider instead (or as well).
 */
data class AiUsageSummary(
    val totalTokensByModel: Map<String, Int>,
    val totalTokens: Int,
)

fun List<PersistedRoundDetails>.toAiUsageSummary(): AiUsageSummary {
    val totalsByModel =
        flatMap { it.aiUsage }
            .groupingBy { it.usage.model }
            .fold(0) { total, entry -> total + entry.usage.totalTokens }
    return AiUsageSummary(
        totalTokensByModel = totalsByModel,
        totalTokens = totalsByModel.values.sum(),
    )
}

/**
 * A round row on its own, as read directly off `risktionary_game_round`. Used
 * as the return of [nz.coreyh.risktionary.game.domain.repository.GameRoundRepository.findByGameId];
 * [PersistedRoundDetails] adds the child records other repositories hold.
 */
data class PersistedRound(
    val id: RoundId,
    val roundNumber: Int,
    val wordId: UUID?,
    val wordValue: String,
    val drawerId: GamePlayerId,
    val startedAt: Instant?,
    val endedAt: Instant,
    val finalState: RoundStateType,
    val abandonedAiCalls: Int,
    val drawerPoints: Int,
)

data class PersistedRoundDetails(
    val round: PersistedRound,
    val guesses: List<GameRoundGuess>,
    val chatMessages: List<PersistedChatMessage>,
    val riskRatings: List<PersistedRiskRating>,
    val drawingAnalyses: List<PersistedDrawingAnalysis>,
    val feedback: List<PersistedFeedback>,
    val aiUsage: List<GameRoundAiUsage>,
)
