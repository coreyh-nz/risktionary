package nz.coreyh.risktionary.words.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ExposedWordSynonymTable : Table("risktionary_word_synonym") {
    val wordId = javaUUID("word_id").references(ExposedWordTable.id, onDelete = ReferenceOption.CASCADE)
    val value = varchar("value", 100)

    override val primaryKey = PrimaryKey(wordId, value)
}
