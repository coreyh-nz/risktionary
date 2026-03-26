package nz.coreyh.risktionary.auth.application.service

import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.service.UserService
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val oAuthAccountService: OAuthAccountService,
) {
    fun authenticateOAuthUser(oauthUserInfo: OAuthUserInfo): User? =
        transaction {
            val (email, firstName, lastName, displayName, provider, providerUserId) = oauthUserInfo
            // try to find user if oauth account is linked
            oAuthAccountService
                .findByProviderIdentity(
                    provider = provider,
                    providerUserId = providerUserId,
                )?.let {
                    return@transaction userService.findById(it.userId)
                }

            // no oauth link - find or create user by email then link
            val user =
                userService.findByEmail(email)
                    ?: userService
                        .create(
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
                            displayName = displayName,
                        )
            oAuthAccountService.linkAccount(
                userId = user.id,
                provider = oauthUserInfo.provider,
                providerUserId = providerUserId,
                email = email,
            )
            user
        }
}
