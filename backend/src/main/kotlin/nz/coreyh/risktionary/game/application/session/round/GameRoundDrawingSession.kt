package nz.coreyh.risktionary.game.application.session.round

import java.util.concurrent.CopyOnWriteArrayList
import nz.coreyh.risktionary.ai.infrastructure.dispatch.AiDispatcher
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisEntry
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
import nz.coreyh.risktionary.game.application.session.LockableSession
import kotlin.time.Instant

/**
 * Holds the periodic drawing screenshots and their AI-generated
 * interpretations for a single round.
 *
 * Owns the [AiDispatcher] the analyses run on. It is closed once the round
 * leaves the drawing phase, after which nothing further is recorded.
 */
class GameRoundDrawingSession(
    private val elapsedMsAt: (Instant) -> Long? = { null },
) : LockableSession() {
    private val analyses = CopyOnWriteArrayList<DrawingAnalysisEntry>()
    private var closed = false

    val aiDispatcher = AiDispatcher()

    /**
     * Records a screenshot and its AI interpretation together, so the two
     * are never observed out of sync with each other.
     *
     * Ignored once the session has been [close]d.
     *
     * @param capturedAt when the screenshot was received.
     */
    fun record(
        imageBytes: ByteArray,
        mimeType: String,
        result: DrawingAnalysisResult,
        capturedAt: Instant,
    ) {
        withLock {
            if (closed) return
            analyses.add(
                DrawingAnalysisEntry(
                    imageBytes = imageBytes,
                    mimeType = mimeType,
                    result = result,
                    capturedAt = capturedAt,
                    elapsedMs = elapsedMsAt(capturedAt),
                ),
            )
        }
    }

    /**
     * Stops accepting analyses and shuts down the dispatcher. Safe to call
     * more than once.
     */
    fun close() {
        withLock {
            if (closed) return
            closed = true
        }
        aiDispatcher.close()
    }

    /**
     * The most recent interpretation, or `null` if no analysis has completed
     * yet for this round.
     */
    fun latestAnalysis(): DrawingAnalysisResult? = analyses.lastOrNull()?.result

    /**
     * Full history of screenshots and interpretations for this round, in order.
     * */
    fun history(): List<DrawingAnalysisEntry> = analyses.toList()
}
