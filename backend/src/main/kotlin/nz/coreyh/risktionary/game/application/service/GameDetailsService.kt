package nz.coreyh.risktionary.game.application.service

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.details.PersistedGameDetails
import nz.coreyh.risktionary.game.domain.model.details.PersistedGameSummary
import nz.coreyh.risktionary.game.domain.model.details.PersistedRoundDetails
import nz.coreyh.risktionary.game.domain.model.details.toAiUsageSummary
import nz.coreyh.risktionary.game.domain.repository.GamePlayerRepository
import nz.coreyh.risktionary.game.domain.repository.GameRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundAiUsageRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundChatMessageRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundDrawingAnalysisRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundFeedbackRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundGuessRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundRiskRatingRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Reads the full persisted record of a game, for research analysis.
 *
 * This is a read-only reporting path: it reads directly off the persistence
 * repositories, not the live in-memory session, and has no effect on gameplay.
 */
@Service
class GameDetailsService(
    private val gameRepository: GameRepository,
    private val gamePlayerRepository: GamePlayerRepository,
    private val gameRoundRepository: GameRoundRepository,
    private val gameRoundGuessRepository: GameRoundGuessRepository,
    private val gameRoundChatMessageRepository: GameRoundChatMessageRepository,
    private val gameRoundRiskRatingRepository: GameRoundRiskRatingRepository,
    private val gameRoundDrawingAnalysisRepository: GameRoundDrawingAnalysisRepository,
    private val gameRoundFeedbackRepository: GameRoundFeedbackRepository,
    private val gameRoundAiUsageRepository: GameRoundAiUsageRepository,
) {
    /** Every persisted game, newest first, for picking one to look up in full. */
    fun listGames(): List<PersistedGameSummary> = gameRepository.findAll()

    /**
     * @throws GameNotFoundException if no game with [gameId] was ever persisted.
     */
    fun getDetails(gameId: GameId): PersistedGameDetails {
        val game = gameRepository.findById(gameId) ?: throw GameNotFoundException()
        val players = gamePlayerRepository.findByGameId(gameId)
        val rounds =
            gameRoundRepository.findByGameId(gameId).map { round ->
                PersistedRoundDetails(
                    round = round,
                    guesses = gameRoundGuessRepository.findByRoundId(round.id),
                    chatMessages = gameRoundChatMessageRepository.findByRoundId(round.id),
                    riskRatings = gameRoundRiskRatingRepository.findByRoundId(round.id),
                    drawingAnalyses = gameRoundDrawingAnalysisRepository.findByRoundId(round.id),
                    feedback = gameRoundFeedbackRepository.findByRoundId(round.id),
                    aiUsage = gameRoundAiUsageRepository.findByRoundId(round.id),
                )
            }

        return PersistedGameDetails(game = game, players = players, rounds = rounds, aiUsageSummary = rounds.toAiUsageSummary())
    }

    /**
     * The image and mime type of one drawing analysis, or null if it does not
     * exist or does not belong to [gameId].
     */
    fun getDrawingImage(
        gameId: GameId,
        analysisId: UUID,
    ) = gameRoundDrawingAnalysisRepository.findImage(gameId, analysisId)
}
