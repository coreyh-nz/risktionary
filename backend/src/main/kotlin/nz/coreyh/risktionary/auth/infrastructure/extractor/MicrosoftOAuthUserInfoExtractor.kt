package nz.coreyh.risktionary.auth.infrastructure.extractor

import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

/**
 * Extracts Microsoft‑specific OAuth user information from an [OidcUser].
 *
 * Microsoft Azure AD and Microsoft personal accounts do not strictly follow
 * the OIDC standard. As a result, several attributes differ from Google or
 * other providers:
 *
 *  - The unique user ID is stored in the `"oid"` claim.
 *  - Email may appear in `"email"` or `"preferred_username"` depending on tenant.
 *  - First and last names use `"givenname"` and `"familyname"` instead of
 *    the standard `"given_name"` and `"family_name"`.
 *  - The `"name"` attribute may be missing, in which case a display name is
 *    constructed from first and last name.
 */
@Component
class MicrosoftOAuthUserInfoExtractor : OAuthUserInfoExtractor {
    override val provider = OAuthProvider.MICROSOFT

    /**
     * Extracts normalized Microsoft OAuth user information.
     *
     * @param user the raw OAuth user returned by Spring Security
     * @return a populated [OAuthUserInfo] instance
     * @throws IllegalArgumentException if required claims are missing
     */
    override fun extract(user: OAuth2User): OAuthUserInfo {
        val user =
            user as? OidcUser
                ?: throw IllegalArgumentException("Microsoft login did not return an OIDC user")
        val id =
            user.getAttribute<String>("oid")
                ?: throw IllegalArgumentException("Microsoft login did not return an OID user")
        val email =
            user.email
                ?: user.getAttribute<String>("preferred_username")
                ?: throw IllegalArgumentException("Microsoft login did not return an email")

        val firstName = user.getAttribute<String>("givenname") ?: ""
        val lastName = user.getAttribute<String>("familyname") ?: ""
        val name =
            user.getAttribute<String>("name")
                ?: listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")

        return OAuthUserInfo(
            email = email,
            firstName = firstName,
            lastName = lastName,
            displayName = name,
            provider = provider,
            providerUserId = id,
        )
    }
}
