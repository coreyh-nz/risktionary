package nz.coreyh.risktionary.auth.domain.repository

import nz.coreyh.risktionary.auth.domain.model.OAuthAccount
import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.user.domain.model.UserId

interface OAuthAccountRepository {
    fun findByProviderIdentity(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount?

    fun findByUserId(userId: UserId): List<OAuthAccount>

    fun create(
        userId: UserId,
        provider: OAuthProvider,
        providerUserId: String,
        email: String,
    ): OAuthAccount
}
