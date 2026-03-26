package nz.coreyh.risktionary.auth.application.service

import nz.coreyh.risktionary.auth.domain.model.OAuthAccount
import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.domain.repository.OAuthAccountRepository
import nz.coreyh.risktionary.user.domain.model.UserId
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class OAuthAccountService(
    private val oAuthAccountRepository: OAuthAccountRepository,
) {
    fun linkAccount(
        userId: UserId,
        provider: OAuthProvider,
        providerUserId: String,
        email: String,
    ): OAuthAccount =
        transaction {
            oAuthAccountRepository.findByProviderIdentity(provider, providerUserId) ?: let {
                oAuthAccountRepository.create(
                    userId = userId,
                    provider = provider,
                    providerUserId = providerUserId,
                    email = email,
                )
            }
        }

    fun findByProviderIdentity(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount? = oAuthAccountRepository.findByProviderIdentity(provider, providerUserId)
}
