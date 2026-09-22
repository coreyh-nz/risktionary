package nz.coreyh.risktionary.support.factory.user

import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.user.domain.model.UserRole
import nz.coreyh.risktionary.user.domain.model.toUserId
import java.util.UUID

fun createTestUserId() = UUID.randomUUID().toUserId()

fun createTestUser(
    id: UserId = createTestUserId(),
    email: String = "jane.doe@gmail.com",
    firstName: String = "Jane",
    lastName: String = "Doe",
    displayName: String = "Jane Doe",
    roles: Set<UserRole> = emptySet(),
): User =
    User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        displayName = displayName,
        roles = roles,
    )
