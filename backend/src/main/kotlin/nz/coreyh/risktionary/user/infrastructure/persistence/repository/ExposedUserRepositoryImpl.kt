package nz.coreyh.risktionary.user.infrastructure.persistence.repository

import java.util.UUID
import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.user.domain.model.toUserId
import nz.coreyh.risktionary.user.domain.repository.UserRepository
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserRoleTable
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedUserRepositoryImpl : UserRepository {
    override fun findById(id: UserId): User? = findById(id.value)

    override fun findByEmail(email: String): User? =
        transaction {
            (ExposedUserTable leftJoin ExposedUserRoleTable)
                .selectAll()
                .where { ExposedUserTable.email eq email }
                .toUser()
        }

    override fun create(
        email: String,
        firstName: String,
        lastName: String,
        displayName: String,
    ): User =
        transaction {
            val id =
                ExposedUserTable
                    .insertAndGetId {
                        it[ExposedUserTable.email] = email
                        it[ExposedUserTable.firstName] = firstName
                        it[ExposedUserTable.lastName] = lastName
                        it[ExposedUserTable.displayName] = displayName
                    }
            findById(id.value)
                ?: throw IllegalStateException("Failed to insert user")
        }

    private fun findById(id: UUID): User? =
        transaction {
            (ExposedUserTable leftJoin ExposedUserRoleTable)
                .selectAll()
                .where { ExposedUserTable.id eq id }
                .toUser()
        }

    private fun Query.toUser(): User? =
        groupBy { it[ExposedUserTable.id] }
            .values
            .map { it.toUserAggregate() }
            .singleOrNull()

    private fun Collection<ResultRow>.toUserAggregate(): User {
        val first = first()
        return User(
            id = first[ExposedUserTable.id].value.toUserId(),
            email = first[ExposedUserTable.email],
            firstName = first[ExposedUserTable.firstName],
            lastName = first[ExposedUserTable.lastName],
            displayName = first[ExposedUserTable.displayName],
            roles = mapNotNull { it.getOrNull(ExposedUserRoleTable.role) }.toSet(),
        )
    }
}
