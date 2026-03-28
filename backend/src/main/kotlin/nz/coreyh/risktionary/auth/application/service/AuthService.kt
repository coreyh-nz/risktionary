package nz.coreyh.risktionary.auth.application.service

import nz.coreyh.risktionary.auth.domain.model.AccessToken
import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import nz.coreyh.risktionary.shared.application.transaction.Transactional
import nz.coreyh.risktionary.user.domain.service.UserService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val oAuthAccountService: OAuthAccountService,
    private val authTokenService: AuthTokenService,
    private val transactional: Transactional,
) {
    fun authenticateOAuthUser(oauthUserInfo: OAuthUserInfo): AccessToken =
        transactional.execute {
            val (email, firstName, lastName, displayName, provider, providerUserId) = oauthUserInfo

            // try to find an existing oauth account link
            val linkedUser =
                oAuthAccountService
                    .findByProviderIdentity(provider, providerUserId)
                    ?.let { oAuthAccount -> userService.findById(oAuthAccount.userId) }

            val user =
                linkedUser ?: run {
                    // otherwise find or create the user then link oauth account
                    val resolvedUser =
                        userService.findByEmail(email)
                            ?: userService
                                .create(
                                    email = email,
                                    firstName = firstName,
                                    lastName = lastName,
                                    displayName = displayName,
                                )
                    oAuthAccountService.linkAccount(
                        userId = resolvedUser.id,
                        provider = provider,
                        providerUserId = providerUserId,
                        email = email,
                    )
                    resolvedUser
                }

            authTokenService.generateAccessToken(user)
        }
}
