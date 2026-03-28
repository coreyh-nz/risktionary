package nz.coreyh.risktionary.auth.infrastructure.extractor

import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

/**
 * Extracts Google‑specific OAuth user information from an [OidcUser].
 *
 * Google fully supports OpenID Connect, so Spring Security exposes most fields
 * through the standard OIDC attributes:
 *
 *  - `sub` → provider user ID
 *  - `email`
 *  - `given_name`
 *  - `family_name`
 *  - `name`
 */
@Component
class GoogleOAuthUserInfoExtractor : OAuthUserInfoExtractor {
    override val provider = OAuthProvider.GOOGLE

    /**
     * Extracts normalized Google OAuth user information.
     *
     * @param user the raw OAuth user returned by Spring Security
     * @return a populated [OAuthUserInfo] instance
     * @throws IllegalArgumentException if the user is not an [OidcUser] or
     *         required fields are missing
     */
    override fun extract(user: OAuth2User): OAuthUserInfo {
        val oidc =
            user as? OidcUser
                ?: throw IllegalArgumentException("Google login did not return an OIDC user")
        val id = user.subject
        val email =
            oidc.email
                ?: throw IllegalArgumentException("Email not found from Google")
        val firstName = oidc.givenName ?: ""
        val lastName = oidc.familyName ?: ""
        val name = oidc.fullName

        return OAuthUserInfo(
            email = email,
            firstName = firstName,
            lastName = lastName,
            displayName = name,
            provider = OAuthProvider.GOOGLE,
            providerUserId = id,
        )
    }
}
