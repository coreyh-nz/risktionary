package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisEntry
import java.util.UUID
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.details.PersistedDrawingAnalysis
import nz.coreyh.risktionary.game.domain.model.details.PersistedDrawingImage
import nz.coreyh.risktionary.game.domain.model.round.RoundId

interface GameRoundDrawingAnalysisRepository {
    /** Inserts [entries], which must be in capture order. */
    fun insertAll(
        roundId: RoundId,
        entries: List<DrawingAnalysisEntry>,
    )

    /** Every analysis captured in [roundId], in capture order. The image bytes are not included. */
    fun findByRoundId(roundId: RoundId): List<PersistedDrawingAnalysis>

    /**
     * The image and its mime type for one analysis, or null if [analysisId] does
     * not exist or belongs to a round of a different game than [gameId].
     */
    fun findImage(
        gameId: GameId,
        analysisId: UUID,
    ): PersistedDrawingImage?
}
