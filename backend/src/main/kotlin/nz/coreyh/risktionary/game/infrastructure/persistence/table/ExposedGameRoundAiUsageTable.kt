package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

/** One row per successful AI call, so tokens can be summed per round, game, player or purpose. */
object ExposedGameRoundAiUsageTable : UUIDTable("risktionary_game_round_ai_usage") {
    val roundId = javaUUID("round_id").references(ExposedGameRoundTable.id, onDelete = ReferenceOption.CASCADE)
    val seq = integer("seq")
    val playerId = javaUUID("player_id").references(ExposedGamePlayerTable.id).nullable()
    val usagePurpose = enumerationByName<AiUsagePurpose>("usage_purpose", 32)
    val provider = varchar("provider", 64).default("unknown")
    val modelName = varchar("model_name", 128)
    val promptTokens = integer("prompt_tokens")
    val completionTokens = integer("completion_tokens")
    val totalTokens = integer("total_tokens")
    val recordedAt = timestamp("recorded_at")
}
