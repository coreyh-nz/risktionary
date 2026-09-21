package nz.coreyh.risktionary.game.application.service.round

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationResult
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGuessContext
import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.feedback.domain.model.GeneratedFeedback
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.feedback.domain.model.createFeedbackId
import nz.coreyh.risktionary.feedback.domain.service.FeedbackService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessageId
import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Generates and delivers AI feedback for a round according to each player's
 * assigned timing condition.
 *
 * - [FeedbackTimingCondition.INSTANT]: after every guess, fact generation is
 *   given all of that player's guesses so far.
 * - [FeedbackTimingCondition.DELAYED]: guesses are only accumulated. When the
 *   drawing phase ends, fact generation runs once per player with all their
 *   guesses.
 *
 * Generation runs on the game's feedback dispatcher, never on the calling
 * thread. Each result is recorded on the round's feedback session and, if it
 * succeeded, sent to the player.
 */
@Service
class GameRoundFeedbackService(
    private val feedbackService: FeedbackService,
    private val gameEventPublisher: GameEventPublisher,
) {
    /**
     * Records [guess] with the round context it was made in and, for an
     * instant-feedback player, generates feedback for it.
     *
     * @param messageId the chat message that carries the guess, which instant
     *    feedback is delivered as a reply to.
     */
    fun onGuess(
        round: GameRoundSession,
        player: GamePlayerSession,
        guess: GameRoundGuess,
        messageId: ChatMessageId,
    ) {
        val game = round.game
        val mode = game.config.feedbackGenerationMode
        if (mode == FeedbackGenerationMode.NONE) return
        if (guess.result == GuessResultType.CORRECT) return

        val assignment = game.feedback.findAssignmentFor(player.id) ?: return

        val guessesSoFar =
            round.feedback.addGuess(
                player.id,
                FeedbackGuessContext(
                    guessId = guess.id,
                    text = guess.text,
                    correct = false,
                    drawingNote = (round.drawing.latestAnalysis() as? DrawingAnalysisResult.Fact)?.note,
                    timeRemainingFraction = timeRemainingFraction(round, guess.submittedAt),
                ),
            )

        if (assignment.timingCondition == FeedbackTimingCondition.INSTANT) {
            generate(round, mode, player.id, assignment, guessesSoFar, messageId)
        }
    }

    /**
     * Generates feedback for every delayed-feedback player who made at least
     * one guess. Called once, when the drawing phase ends.
     */
    fun generateDelayed(round: GameRoundSession) {
        val game = round.game
        val mode = game.config.feedbackGenerationMode
        if (mode == FeedbackGenerationMode.NONE) return

        round.feedback.getGuessesByPlayer().forEach { (playerId, guesses) ->
            val assignment = game.feedback.findAssignmentFor(playerId) ?: return@forEach
            if (assignment.timingCondition == FeedbackTimingCondition.DELAYED) {
                generate(round, mode, playerId, assignment, guesses, messageId = null)
            }
        }
    }

    private fun generate(
        round: GameRoundSession,
        mode: FeedbackGenerationMode,
        playerId: GamePlayerId,
        assignment: GamePlayerFeedbackAssignment,
        guesses: List<FeedbackGuessContext>,
        messageId: ChatMessageId?,
    ) {
        val job =
            round.game.feedback.feedbackDispatcher.launch(
                block = {
                    feedbackService.generate(
                        mode = mode,
                        payload =
                            FeedbackFactPayload(
                                guesses = guesses,
                                word = round.word,
                                condition = assignment.framingCondition,
                                timing = assignment.timingCondition,
                            ),
                    )
                },
                onResult = { result -> handleResult(round, playerId, assignment, guesses, messageId, result) },
                onError = { e ->
                    logger.error(e) { "Feedback generation failed (round=${round.id}, player=$playerId)" }
                },
            )
        round.feedback.track(job)
    }

    private fun handleResult(
        round: GameRoundSession,
        playerId: GamePlayerId,
        assignment: GamePlayerFeedbackAssignment,
        guesses: List<FeedbackGuessContext>,
        messageId: ChatMessageId?,
        result: FeedbackGenerationResult,
    ) {
        if (round.feedback.isSettled) {
            logger.warn { "Discarding feedback that finished after the round was saved (round=${round.id}, player=$playerId)" }
            return
        }

        result.usage.forEach { round.aiUsage.record(it, playerId) }

        val feedback =
            GeneratedFeedback(
                id = createFeedbackId(),
                playerId = playerId,
                sourceGuessIds = guesses.map { it.guessId },
                framingCondition = assignment.framingCondition,
                timingCondition = assignment.timingCondition,
                status = result.status,
                factText = result.factText,
                framedText = result.framedText,
                generatedAt = result.generatedAt,
            )
        round.feedback.record(feedback)

        val text = result.framedText
        if (result.status == FeedbackGenerationStatus.SUCCESS && text != null) {
            gameEventPublisher.publishRoundFeedback(
                playerId = playerId,
                feedbackId = feedback.id,
                messageId = messageId,
                roundNumber = round.game.roundNumber,
                text = text,
            )
        }
    }

    /**
     * How much of the drawing phase was left at [at]: 1.0 at the start down
     * to 0.0 at the end. Null if the phase has no timer.
     */
    private fun timeRemainingFraction(
        round: GameRoundSession,
        at: Instant,
    ): Double? {
        val window = (round.state as? GameRoundState.InProgress)?.phase?.timeWindow ?: return null
        val total = window.duration.inWholeMilliseconds
        if (total <= 0) return null
        return ((window.endingAt - at).inWholeMilliseconds.toDouble() / total).coerceIn(0.0, 1.0)
    }
}
