package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID

/** Keyed by the in-game player id only; deliberately holds no identity data. */
object ExposedGamePlayerTable : UUIDTable("risktionary_game_player") {
    val gameId = javaUUID("game_id").references(ExposedGameTable.id, onDelete = ReferenceOption.CASCADE)
    val feedbackFramingCondition = enumerationByName<FeedbackFramingCondition>("feedback_framing_condition", 16).nullable()
    val feedbackTimingCondition = enumerationByName<FeedbackTimingCondition>("feedback_timing_condition", 16).nullable()
}
