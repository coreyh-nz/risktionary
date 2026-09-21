package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ExposedGameRoundFeedbackTable : UUIDTable("risktionary_game_round_feedback") {
    val roundId = javaUUID("round_id").references(ExposedGameRoundTable.id, onDelete = ReferenceOption.CASCADE)
    val playerId = javaUUID("player_id").references(ExposedGamePlayerTable.id)
    val framingCondition = enumerationByName<FeedbackFramingCondition>("framing_condition", 16)
    val timingCondition = enumerationByName<FeedbackTimingCondition>("timing_condition", 16)
    val status = enumerationByName<FeedbackGenerationStatus>("status", 16)
    val factText = text("fact_text").nullable()
    val framedText = text("framed_text").nullable()
    val generatedAt = timestamp("generated_at")
}
