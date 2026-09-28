package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

/** The provider, model and temperature each AI use case ran with in a game. */
object ExposedGameAiUseCaseTable : Table("risktionary_game_ai_use_case") {
    val gameId = javaUUID("game_id").references(ExposedGameTable.id, onDelete = ReferenceOption.CASCADE)
    val usagePurpose = enumerationByName<AiUsagePurpose>("usage_purpose", 32)
    val provider = varchar("provider", 64)
    val modelName = varchar("model_name", 128)
    val temperature = double("temperature")

    override val primaryKey = PrimaryKey(gameId, usagePurpose)
}
