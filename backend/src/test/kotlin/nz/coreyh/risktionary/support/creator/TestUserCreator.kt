package nz.coreyh.risktionary.support.creator

import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.service.UserService
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Component

@Component
class TestUserCreator(
    private val userService: UserService,
) {
    fun createTestUser(
        firstName: String = "John",
        lastName: String = "Smith",
        displayName: String = "$firstName $lastName",
        email: String = "john@smith.com",
    ): User =
        transaction {
            userService.create(
                email = email,
                firstName = firstName,
                lastName = lastName,
                displayName = displayName,
            )
            requireNotNull(userService.findByEmail(email))
        }
}
