package nz.coreyh.risktionary.auth.domain.model

data class OAuthUserInfo(
    val email: String,
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val provider: OAuthProvider,
    val providerUserId: String,
)
