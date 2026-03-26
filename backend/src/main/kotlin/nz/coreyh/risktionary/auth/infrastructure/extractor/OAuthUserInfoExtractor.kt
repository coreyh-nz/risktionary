package nz.coreyh.risktionary.auth.infrastructure.extractor

import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * Extracts provider‑specific OAuth user information from a Spring Security [OAuth2User].
 *
 * Each OAuth provider exposes user attributes differently.
 * Implementations of this interface normalize those provider‑specific fields into a
 * consistent [OAuthUserInfo] model used by the application.
 */
interface OAuthUserInfoExtractor {
    /** The OAuth provider this extractor supports. */
    val provider: OAuthProvider

    /**
     * Extracts normalized OAuth user information from the raw provider user object.
     *
     * @param user the raw OAuth user returned by Spring Security
     * @return a normalized [OAuthUserInfo] instance
     * @throws IllegalArgumentException if the user is not of the expected type or
     *         required attributes are missing
     */
    fun extract(user: OAuth2User): OAuthUserInfo
}
