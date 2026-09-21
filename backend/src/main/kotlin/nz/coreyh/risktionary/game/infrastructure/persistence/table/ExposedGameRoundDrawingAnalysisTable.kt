package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResultType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ExposedGameRoundDrawingAnalysisTable : UUIDTable("risktionary_game_round_drawing_analysis") {
    val roundId = javaUUID("round_id").references(ExposedGameRoundTable.id, onDelete = ReferenceOption.CASCADE)
    val seq = integer("seq")
    val capturedAt = timestamp("captured_at")
    val elapsedMs = long("elapsed_ms").nullable()
    val image = binary("image")
    val imageMimeType = varchar("image_mime_type", 64)
    val resultType = enumerationByName<DrawingAnalysisResultType>("result_type", 16)
    val factNote = text("fact_note").nullable()
}
