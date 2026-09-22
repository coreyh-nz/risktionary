package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResultType
import java.util.UUID
import kotlin.time.Instant

/** The screenshot's own bytes are not included; fetch them by [id] separately. */
data class PersistedDrawingAnalysis(
    val id: UUID,
    val seq: Int,
    val capturedAt: Instant,
    val elapsedMs: Long?,
    val imageMimeType: String,
    val resultType: DrawingAnalysisResultType,
    val factNote: String?,
)

data class PersistedDrawingImage(
    val mimeType: String,
    val bytes: ByteArray,
)
