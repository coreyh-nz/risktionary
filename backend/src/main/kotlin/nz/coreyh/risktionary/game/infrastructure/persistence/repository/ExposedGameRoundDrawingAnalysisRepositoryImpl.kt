package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisEntry
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
import nz.coreyh.risktionary.feedback.domain.model.analysis.type
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.repository.GameRoundDrawingAnalysisRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundDrawingAnalysisTable
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGameRoundDrawingAnalysisRepositoryImpl : GameRoundDrawingAnalysisRepository {
    override fun insertAll(
        roundId: RoundId,
        entries: List<DrawingAnalysisEntry>,
    ) {
        if (entries.isEmpty()) return
        transaction {
            ExposedGameRoundDrawingAnalysisTable.batchInsert(entries.withIndex()) { (index, entry) ->
                this[ExposedGameRoundDrawingAnalysisTable.roundId] = roundId.value
                this[ExposedGameRoundDrawingAnalysisTable.seq] = index
                this[ExposedGameRoundDrawingAnalysisTable.capturedAt] = entry.capturedAt
                this[ExposedGameRoundDrawingAnalysisTable.elapsedMs] = entry.elapsedMs
                this[ExposedGameRoundDrawingAnalysisTable.image] = entry.imageBytes
                this[ExposedGameRoundDrawingAnalysisTable.imageMimeType] = entry.mimeType
                this[ExposedGameRoundDrawingAnalysisTable.resultType] = entry.result.type
                this[ExposedGameRoundDrawingAnalysisTable.factNote] = (entry.result as? DrawingAnalysisResult.Fact)?.note
            }
        }
    }
}
