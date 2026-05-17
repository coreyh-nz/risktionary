package nz.coreyh.risktionary.words.infrastructure.persistence.table

import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ExposedWordTable : UUIDTable("risktionary_word") {
    val value = varchar("value", 100)
    val descriptionText = text("description_text")
    val descriptionContent = text("description_content")
    val createdBy = javaUUID("created_by").references(ExposedUserTable.id)
    val createdAt = timestamp("created_at")
}
