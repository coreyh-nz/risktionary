package nz.coreyh.risktionary.support.factory.auth

import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import nz.coreyh.risktionary.support.factory.user.createTestUser
import nz.coreyh.risktionary.user.domain.model.User

fun createTestOAuthUserInfo(
    user: User = createTestUser(),
    provider: OAuthProvider = OAuthProvider.GOOGLE,
    providerUserId: String = "${provider.id}-${user.id}",
): OAuthUserInfo =
    OAuthUserInfo(
        email = user.email,
        firstName = user.firstName,
        lastName = user.lastName,
        displayName = user.displayName,
        provider = OAuthProvider.GOOGLE,
        providerUserId = providerUserId,
    )
