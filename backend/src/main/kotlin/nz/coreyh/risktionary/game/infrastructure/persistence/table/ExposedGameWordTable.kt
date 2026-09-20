package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.words.infrastructure.persistence.table.ExposedWordTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ExposedGameWordTable : Table("risktionary_game_word") {
    val gameId = javaUUID("game_id").references(ExposedGameTable.id, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val wordId = javaUUID("word_id").references(ExposedWordTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val wordValue = varchar("word_value", 100)

    override val primaryKey = PrimaryKey(gameId, position)
}
