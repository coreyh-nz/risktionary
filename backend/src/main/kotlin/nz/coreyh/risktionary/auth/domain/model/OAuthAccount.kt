package nz.coreyh.risktionary.auth.domain.model

import nz.coreyh.risktionary.user.domain.model.UserId

data class OAuthAccount(
    val userId: UserId,
    val provider: OAuthProvider,
    val providerUserId: String,
    val email: String,
)
