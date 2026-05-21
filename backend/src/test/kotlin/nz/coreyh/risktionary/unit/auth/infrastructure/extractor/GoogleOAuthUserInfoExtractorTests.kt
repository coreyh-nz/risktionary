package nz.coreyh.risktionary.unit.auth.infrastructure.extractor

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.infrastructure.extractor.GoogleOAuthUserInfoExtractor
import nz.coreyh.risktionary.support.annotation.MockKTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User

@MockKTest
class GoogleOAuthUserInfoExtractorTests {
    private lateinit var extractor: GoogleOAuthUserInfoExtractor

    @BeforeEach
    fun setup() {
        extractor = GoogleOAuthUserInfoExtractor()
    }

    @Test
    fun `extract returns returns oauth user info when oidc user contains all fields`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.subject } returns "google-123"
        every { oidcUser.email } returns "jane@example.com"
        every { oidcUser.givenName } returns "Jane"
        every { oidcUser.familyName } returns "Doe"
        every { oidcUser.fullName } returns "Jane Doe"

        val result = extractor.extract(oidcUser)

        result.email shouldBe "jane@example.com"
        result.firstName shouldBe "Jane"
        result.lastName shouldBe "Doe"
        result.displayName shouldBe "Jane Doe"
        result.provider shouldBe OAuthProvider.GOOGLE
        result.providerUserId shouldBe "google-123"
    }

    @Test
    fun `extract throws when user is not an oidc user`() {
        val oauthUser = mockk<OAuth2User>()

        shouldThrow<IllegalArgumentException> {
            extractor.extract(oauthUser)
        }
    }

    @Test
    fun `extract throws when oidc user has no email`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.subject } returns "google-123"
        every { oidcUser.email } returns null

        shouldThrow<IllegalArgumentException> {
            extractor.extract(oidcUser)
        }
    }

    @Test
    fun `extract uses empty strings when first or last name is missing`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.subject } returns "google-123"
        every { oidcUser.email } returns "jane@example.com"
        every { oidcUser.givenName } returns null
        every { oidcUser.familyName } returns null
        every { oidcUser.fullName } returns "Jane Doe"

        val result = extractor.extract(oidcUser)

        result.firstName shouldBe ""
        result.lastName shouldBe ""
    }
}
