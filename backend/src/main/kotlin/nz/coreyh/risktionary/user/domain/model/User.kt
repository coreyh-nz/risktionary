package nz.coreyh.risktionary.user.domain.model

data class User(
    val id: UserId,
    val email: String,
    val firstName: String,
    val lastName: String,
    val displayName: String,
)
