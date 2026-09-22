package nz.coreyh.risktionary.user.infrastructure.persistence.table

import nz.coreyh.risktionary.user.domain.model.UserRole
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ExposedUserRoleTable : Table("risktionary_user_role") {
    val userId = javaUUID("user_id").references(ExposedUserTable.id, onDelete = ReferenceOption.CASCADE)
    val role = enumerationByName<UserRole>("user_role", 32)

    override val primaryKey = PrimaryKey(userId, role)
}
