package nz.coreyh.risktionary.unit.auth.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import nz.coreyh.risktionary.auth.application.service.AuthTokenService
import nz.coreyh.risktionary.auth.config.JwtProperties
import nz.coreyh.risktionary.shared.application.service.TokenService
import nz.coreyh.risktionary.shared.domain.model.Token
import nz.coreyh.risktionary.support.factory.user.createTestUser
import nz.coreyh.risktionary.user.domain.model.UserRole
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class AuthTokenServiceTests {
    private val tokenService = mockk<TokenService>()
    private val authTokenService = AuthTokenService(tokenService, JwtProperties(secret = "secret", accessLifetime = java.time.Duration.ofDays(30)))

    private fun fakeToken(
        subject: String,
        claims: Map<String, String>,
    ): Token {
        val now = Clock.System.now()
        return Token(value = "jwt", subject = subject, issuedAt = now, expiresAt = now + 30.days, claims = claims)
    }

    @Test
    fun `generating an access token encodes the user's roles as a claim`() {
        val user = createTestUser(roles = setOf(UserRole.RESEARCHER))
        val claims = slot<Map<String, String>>()
        every {
            tokenService.generateToken(any(), any(), any(), any(), capture(claims))
        } answers { fakeToken(user.id.toString(), claims.captured) }

        val accessToken = authTokenService.generateAccessToken(user)

        claims.captured["roles"] shouldBe "RESEARCHER"
        accessToken.roles shouldBe setOf(UserRole.RESEARCHER)
    }

    @Test
    fun `generating an access token for a user with no roles encodes no roles`() {
        val user = createTestUser(roles = emptySet())
        val claims = slot<Map<String, String>>()
        every {
            tokenService.generateToken(any(), any(), any(), any(), capture(claims))
        } answers { fakeToken(user.id.toString(), claims.captured) }

        val accessToken = authTokenService.generateAccessToken(user)

        claims.captured["roles"] shouldBe ""
        accessToken.roles shouldBe emptySet()
    }

    @Test
    fun `decoding an access token recovers the roles it was issued with`() {
        val user = createTestUser(roles = setOf(UserRole.RESEARCHER))
        every {
            tokenService.decodeToken("issued-token", AuthTokenService.TOKEN_TYPE)
        } returns fakeToken(user.id.toString(), mapOf("roles" to "RESEARCHER"))

        val decoded = authTokenService.decodeAccessToken("issued-token")

        decoded.roles shouldBe setOf(UserRole.RESEARCHER)
        decoded.userId shouldBe user.id
    }

    @Test
    fun `decoding a token with an unknown role name ignores that role`() {
        val user = createTestUser()
        every {
            tokenService.decodeToken(any(), any())
        } returns fakeToken(user.id.toString(), mapOf("roles" to "RESEARCHER,SOME_REMOVED_ROLE"))

        authTokenService.decodeAccessToken("token").roles shouldBe setOf(UserRole.RESEARCHER)
    }

    @Test
    fun `decoding a token with no roles claim yields no roles`() {
        val user = createTestUser()
        every {
            tokenService.decodeToken(any(), any())
        } returns fakeToken(user.id.toString(), emptyMap())

        authTokenService.decodeAccessToken("token").roles shouldBe emptySet()
    }
}
