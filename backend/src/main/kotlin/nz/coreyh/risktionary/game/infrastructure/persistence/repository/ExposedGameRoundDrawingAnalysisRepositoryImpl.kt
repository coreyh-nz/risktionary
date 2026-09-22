package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import java.util.UUID
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisEntry
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
import nz.coreyh.risktionary.feedback.domain.model.analysis.type
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.details.PersistedDrawingAnalysis
import nz.coreyh.risktionary.game.domain.model.details.PersistedDrawingImage
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.repository.GameRoundDrawingAnalysisRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundDrawingAnalysisTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
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

    override fun findByRoundId(roundId: RoundId): List<PersistedDrawingAnalysis> =
        transaction {
            ExposedGameRoundDrawingAnalysisTable
                .select(
                    ExposedGameRoundDrawingAnalysisTable.id,
                    ExposedGameRoundDrawingAnalysisTable.seq,
                    ExposedGameRoundDrawingAnalysisTable.capturedAt,
                    ExposedGameRoundDrawingAnalysisTable.elapsedMs,
                    ExposedGameRoundDrawingAnalysisTable.imageMimeType,
                    ExposedGameRoundDrawingAnalysisTable.resultType,
                    ExposedGameRoundDrawingAnalysisTable.factNote,
                ).where { ExposedGameRoundDrawingAnalysisTable.roundId eq roundId.value }
                .orderBy(ExposedGameRoundDrawingAnalysisTable.seq, SortOrder.ASC)
                .map {
                    PersistedDrawingAnalysis(
                        id = it[ExposedGameRoundDrawingAnalysisTable.id].value,
                        seq = it[ExposedGameRoundDrawingAnalysisTable.seq],
                        capturedAt = it[ExposedGameRoundDrawingAnalysisTable.capturedAt],
                        elapsedMs = it[ExposedGameRoundDrawingAnalysisTable.elapsedMs],
                        imageMimeType = it[ExposedGameRoundDrawingAnalysisTable.imageMimeType],
                        resultType = it[ExposedGameRoundDrawingAnalysisTable.resultType],
                        factNote = it[ExposedGameRoundDrawingAnalysisTable.factNote],
                    )
                }
        }

    override fun findImage(
        gameId: GameId,
        analysisId: UUID,
    ): PersistedDrawingImage? =
        transaction {
            (ExposedGameRoundDrawingAnalysisTable innerJoin ExposedGameRoundTable)
                .select(ExposedGameRoundDrawingAnalysisTable.image, ExposedGameRoundDrawingAnalysisTable.imageMimeType)
                .where { (ExposedGameRoundDrawingAnalysisTable.id eq analysisId) and (ExposedGameRoundTable.gameId eq gameId.value) }
                .map {
                    PersistedDrawingImage(
                        mimeType = it[ExposedGameRoundDrawingAnalysisTable.imageMimeType],
                        bytes = it[ExposedGameRoundDrawingAnalysisTable.image],
                    )
                }.singleOrNull()
        }
}
