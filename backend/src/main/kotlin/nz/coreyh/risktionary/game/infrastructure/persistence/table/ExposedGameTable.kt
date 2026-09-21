package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ExposedGameTable : UUIDTable("risktionary_game") {
    val code = varchar("code", 16)
    val hostUserId = javaUUID("host_user_id").references(ExposedUserTable.id)
    val createdAt = timestamp("created_at")
    val feedbackGenerationMode = enumerationByName<FeedbackGenerationMode>("feedback_generation_mode", 16)
    val lobbyCountdownMs = long("lobby_countdown_ms")
    val skippingCountdownsEnabled = bool("skipping_countdowns_enabled")
    val endReason = enumerationByName<GameEndReason>("end_reason", 16).nullable()
    val endedAt = timestamp("ended_at").nullable()
}
