package nz.coreyh.risktionary.support.factory.auth

import nz.coreyh.risktionary.auth.domain.model.OAuthAccount
import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import nz.coreyh.risktionary.support.factory.user.createTestUser
import nz.coreyh.risktionary.user.domain.model.User

fun createTestOAuthAccount(
    user: User = createTestUser(),
    oauthUserInfo: OAuthUserInfo = createTestOAuthUserInfo(user = user),
): OAuthAccount =
    OAuthAccount(
        userId = user.id,
        provider = oauthUserInfo.provider,
        providerUserId = oauthUserInfo.providerUserId,
        email = user.email,
    )
