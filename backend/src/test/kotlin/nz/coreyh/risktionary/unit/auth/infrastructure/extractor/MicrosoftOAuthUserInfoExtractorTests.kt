package nz.coreyh.risktionary.unit.auth.infrastructure.extractor

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.infrastructure.extractor.MicrosoftOAuthUserInfoExtractor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User

class MicrosoftOAuthUserInfoExtractorTests {
    private lateinit var extractor: MicrosoftOAuthUserInfoExtractor

    @BeforeEach
    fun setup() {
        extractor = MicrosoftOAuthUserInfoExtractor()
    }

    @Test
    fun `extract returns returns oauth user info when oidc user contains all fields`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.getAttribute<String>("oid") } returns "ms-123"
        every { oidcUser.email } returns "jane@example.com"
        every { oidcUser.getAttribute<String>("givenname") } returns "Jane"
        every { oidcUser.getAttribute<String>("familyname") } returns "Doe"
        every { oidcUser.getAttribute<String>("name") } returns "Jane Doe"

        val result = extractor.extract(oidcUser)

        result.email shouldBe "jane@example.com"
        result.firstName shouldBe "Jane"
        result.lastName shouldBe "Doe"
        result.displayName shouldBe "Jane Doe"
        result.provider shouldBe OAuthProvider.MICROSOFT
        result.providerUserId shouldBe "ms-123"
    }

    @Test
    fun `extract throws when user is not an oidc user`() {
        val oauthUser = mockk<OAuth2User>()

        shouldThrow<IllegalArgumentException> {
            extractor.extract(oauthUser)
        }
    }

    @Test
    fun `extract throws when oid attribute is missing`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.getAttribute<String>("oid") } returns null
        every { oidcUser.email } returns "jane@example.com"

        shouldThrow<IllegalArgumentException> {
            extractor.extract(oidcUser)
        }
    }

    @Test
    fun `extract uses preferred_username when email is missing`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.getAttribute<String>("oid") } returns "ms-123"
        every { oidcUser.email } returns null
        every { oidcUser.getAttribute<String>("preferred_username") } returns "jane@contoso.com"
        every { oidcUser.getAttribute<String>("givenname") } returns "Jane"
        every { oidcUser.getAttribute<String>("familyname") } returns "Doe"
        every { oidcUser.getAttribute<String>("name") } returns "Jane Doe"

        val result = extractor.extract(oidcUser)

        result.email shouldBe "jane@contoso.com"
    }

    @Test
    fun `extract throws when both email and preferred_username are missing`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.getAttribute<String>("oid") } returns "ms-123"
        every { oidcUser.email } returns null
        every { oidcUser.getAttribute<String>("preferred_username") } returns null

        shouldThrow<IllegalArgumentException> {
            extractor.extract(oidcUser)
        }
    }

    @Test
    fun `extract uses empty strings when first or last name is missing`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.getAttribute<String>("oid") } returns "ms-123"
        every { oidcUser.email } returns "jane@example.com"
        every { oidcUser.getAttribute<String>("givenname") } returns null
        every { oidcUser.getAttribute<String>("familyname") } returns null
        every { oidcUser.getAttribute<String>("name") } returns "Jane Doe"

        val result = extractor.extract(oidcUser)

        result.firstName shouldBe ""
        result.lastName shouldBe ""
    }

    @Test
    fun `extract builds display name from first and last name when name attribute is missing`() {
        val oidcUser = mockk<OidcUser>()
        every { oidcUser.getAttribute<String>("oid") } returns "ms-123"
        every { oidcUser.email } returns "jane@example.com"
        every { oidcUser.getAttribute<String>("givenname") } returns "Jane"
        every { oidcUser.getAttribute<String>("familyname") } returns "Doe"
        every { oidcUser.getAttribute<String>("name") } returns null

        val result = extractor.extract(oidcUser)

        result.displayName shouldBe "Jane Doe"
    }
}
