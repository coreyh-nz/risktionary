package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisEntry
import nz.coreyh.risktionary.game.domain.model.round.RoundId

interface GameRoundDrawingAnalysisRepository {
    /** Inserts [entries], which must be in capture order. */
    fun insertAll(
        roundId: RoundId,
        entries: List<DrawingAnalysisEntry>,
    )
}
