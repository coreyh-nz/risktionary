package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.words.infrastructure.persistence.table.ExposedWordTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ExposedGameRoundTable : UUIDTable("risktionary_game_round") {
    val gameId = javaUUID("game_id").references(ExposedGameTable.id, onDelete = ReferenceOption.CASCADE)
    val roundNumber = integer("round_number")
    val wordId = javaUUID("word_id").references(ExposedWordTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val wordValue = varchar("word_value", 100)
    val drawerId = javaUUID("drawer_id").references(ExposedGamePlayerTable.id)
    val startedAt = timestamp("started_at").nullable()
    val endedAt = timestamp("ended_at")
    val finalState = enumerationByName<RoundStateType>("final_state", 16)
    val abandonedAiCalls = integer("abandoned_ai_calls").default(0)

    init {
        uniqueIndex(gameId, roundNumber)
    }
}
