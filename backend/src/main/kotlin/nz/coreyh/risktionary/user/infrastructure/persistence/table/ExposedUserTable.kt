package nz.coreyh.risktionary.user.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

object ExposedUserTable : UUIDTable("risktionary_user") {
    val email = varchar("email", 256).uniqueIndex()
    val firstName = varchar("first_name", 64)
    val lastName = varchar("last_name", 64)
    val displayName = varchar("display_name", 136)
}
