package nz.coreyh.risktionary.user.web.dto

import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.user.domain.model.UserRole

data class UserDetailsDto(
    val id: UserId,
    val email: String,
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val roles: Set<UserRole>,
)

fun User.toDto(): UserDetailsDto =
    UserDetailsDto(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        displayName = displayName,
        roles = roles,
    )
