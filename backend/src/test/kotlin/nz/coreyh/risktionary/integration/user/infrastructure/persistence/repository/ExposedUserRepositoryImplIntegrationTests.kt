package nz.coreyh.risktionary.integration.user.infrastructure.persistence.repository

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestUserCreator
import nz.coreyh.risktionary.user.domain.model.UserRole
import nz.coreyh.risktionary.user.domain.repository.UserRepository
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserRoleTable
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
@Transactional
@SpringBootTest
class ExposedUserRepositoryImplIntegrationTests(
    private val userRepository: UserRepository,
    private val testUserCreator: TestUserCreator,
) {
    @Test
    fun `a user with no roles is read back with an empty role set`() {
        val user = testUserCreator.createUniqueTestUser()

        userRepository.findById(user.id).shouldNotBeNull().roles shouldBe emptySet()
    }

    @Test
    fun `a role granted directly in the database is read back on the user`() {
        val user = testUserCreator.createUniqueTestUser()
        transaction {
            ExposedUserRoleTable.insert {
                it[ExposedUserRoleTable.userId] = user.id.value
                it[ExposedUserRoleTable.role] = UserRole.RESEARCHER
            }
        }

        userRepository.findById(user.id).shouldNotBeNull().roles shouldBe setOf(UserRole.RESEARCHER)
        userRepository.findByEmail(user.email).shouldNotBeNull().roles shouldBe setOf(UserRole.RESEARCHER)
    }
}
