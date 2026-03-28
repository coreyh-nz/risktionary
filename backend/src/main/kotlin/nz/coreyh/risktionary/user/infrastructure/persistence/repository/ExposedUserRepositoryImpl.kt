package nz.coreyh.risktionary.user.infrastructure.persistence.repository

import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.user.domain.model.toUserId
import nz.coreyh.risktionary.user.domain.repository.UserRepository
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserTable
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserTable.id
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ExposedUserRepositoryImpl : UserRepository {
    override fun findById(id: UserId): User? = findById(id.value)

    override fun findByEmail(email: String): User? =
        transaction {
            ExposedUserTable
                .selectAll()
                .where { ExposedUserTable.email eq email }
                .map { it.toDomain() }
                .firstOrNull()
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
            ExposedUserTable
                .selectAll()
                .where { ExposedUserTable.id eq id }
                .map { it.toDomain() }
                .firstOrNull()
        }

    private fun ResultRow.toDomain(): User =
        User(
            id = this[id].value.toUserId(),
            email = this[ExposedUserTable.email],
            firstName = this[ExposedUserTable.firstName],
            lastName = this[ExposedUserTable.lastName],
            displayName = this[ExposedUserTable.displayName],
        )
}
