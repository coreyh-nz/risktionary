package nz.coreyh.risktionary.user.web.dto

import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.UserId

data class UserDetailsDto(
    val id: UserId,
    val email: String,
    val firstName: String,
    val lastName: String,
    val displayName: String,
)

fun User.toDto(): UserDetailsDto =
    UserDetailsDto(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        displayName = displayName,
    )
