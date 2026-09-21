package nz.coreyh.risktionary.game.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.game.domain.repository.GamePlayerRepository
import nz.coreyh.risktionary.game.domain.repository.GameRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundAiUsageRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundChatMessageRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundDrawingAnalysisRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundFeedbackRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundGuessRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundRiskRatingRepository
import nz.coreyh.risktionary.shared.application.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

/**
 * Writes a record of what happened in a game to the database, for later
 * research analysis.
 *
 * The in-memory sessions remain the source of truth during a game. This
 * service only reads them and writes, synchronously, so any failure is
 * thrown to the caller rather than being swallowed. There are no retries.
 *
 * Players are recorded by [nz.coreyh.risktionary.game.domain.model.player.GamePlayerId]
 * only; no identity data (display names, user ids) is persisted.
 */
@Service
class GameResearchPersistenceService(
    private val transactional: Transactional,
    private val gameRepository: GameRepository,
    private val gamePlayerRepository: GamePlayerRepository,
    private val gameRoundRepository: GameRoundRepository,
    private val gameRoundGuessRepository: GameRoundGuessRepository,
    private val gameRoundChatMessageRepository: GameRoundChatMessageRepository,
    private val gameRoundRiskRatingRepository: GameRoundRiskRatingRepository,
    private val gameRoundDrawingAnalysisRepository: GameRoundDrawingAnalysisRepository,
    private val gameRoundFeedbackRepository: GameRoundFeedbackRepository,
    private val gameRoundAiUsageRepository: GameRoundAiUsageRepository,
    private val clock: Clock = Clock.System,
) {
    /**
     * Records the game, its configuration, and its words. Called when the
     * session is created, since every later row references the game.
     */
    fun persistGame(game: GameSession) {
        gameRepository.create(
            id = game.id,
            code = game.code,
            hostId = game.host.id,
            createdAt = game.createdAt,
            config = game.config,
        )
        logger.debug { "Persisted game (game=${game.id})" }
    }

    /**
     * Records everything that happened in [round], along with any players in
     * the game not yet recorded (players can join at any time), in a single
     * transaction.
     *
     * @param abandonedAiCalls AI calls that were still running and were given up on, recorded on the round.
     */
    fun persistRound(
        round: GameRoundSession,
        abandonedAiCalls: Int = 0,
    ) {
        val game = round.game
        val drawer = round.requireState<GameRoundState.InProgress>().drawer

        transactional.execute {
            gamePlayerRepository.insertMissing(
                gameId = game.id,
                players =
                    game.getPlayers().associate { player ->
                        player.id to
                            when (game.config.feedbackGenerationEnabled) {
                                true -> game.feedback.findAssignmentFor(player.id)
                                false -> null
                            }
                    },
            )

            gameRoundRepository.create(
                id = round.id,
                gameId = game.id,
                roundNumber = game.roundNumber,
                word = round.word,
                drawerId = drawer.id,
                startedAt = round.drawingStartedAt,
                endedAt = clock.now(),
                finalState = round.state.type,
                abandonedAiCalls = abandonedAiCalls,
            )

            // guesses must be inserted before the chat messages and feedback that reference them
            gameRoundGuessRepository.insertAll(round.id, round.guesses.getGuesses())
            gameRoundChatMessageRepository.insertAll(round.id, round.getMessages())
            gameRoundRiskRatingRepository.insertAll(round.id, round.riskRatings.getRatingHistory())
            gameRoundDrawingAnalysisRepository.insertAll(round.id, round.drawing.history())
            gameRoundFeedbackRepository.insertAll(round.id, round.feedback.getAll())
            gameRoundAiUsageRepository.insertAll(round.id, round.aiUsage.getAll())
        }

        logger.debug { "Persisted round (game=${game.id}, round=${round.id})" }
    }

    /**
     * Marks an already persisted round as completed.
     */
    fun markRoundCompleted(round: GameRoundSession) {
        gameRoundRepository.updateFinalState(round.id, RoundStateType.COMPLETED)
    }

    fun markGameEnded(
        game: GameSession,
        reason: GameEndReason,
    ) {
        gameRepository.markEnded(game.id, reason, clock.now())
        logger.debug { "Marked persisted game ended (game=${game.id}, reason=$reason)" }
    }
}
