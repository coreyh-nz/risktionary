package nz.coreyh.risktionary.unit.auth.infrastructure.security

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.Cookie
import nz.coreyh.risktionary.auth.application.service.AuthTokenService
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.auth.config.CookieProperties
import nz.coreyh.risktionary.auth.domain.model.AccessToken
import nz.coreyh.risktionary.auth.infrastructure.security.JwtAuthenticationFilter
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.model.UserRole
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.Cookie.SameSite
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class JwtAuthenticationFilterTests {
    private val authTokenService = mockk<AuthTokenService>()
    private val cookieProperties =
        CookieProperties(
            path = "/",
            secure = false,
            sameSite = SameSite.LAX,
            accessTokenName = "risktionary_at",
            loginSuccessRedirectUrlName = "risktionary_lr",
        )
    private val filter = JwtAuthenticationFilter(authTokenService, AuthConfiguration(cookieProperties))

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    private fun runFilter(cookieValue: String) {
        val request = MockHttpServletRequest()
        request.setCookies(Cookie(cookieProperties.accessTokenName, cookieValue))
        filter.doFilter(request, MockHttpServletResponse(), MockFilterChain())
    }

    @Test
    fun `the authenticated context carries the authorities of the decoded token's roles`() {
        val userId = createTestUserId()
        val now = Clock.System.now()
        every { authTokenService.decodeAccessToken("valid-token") } returns
            AccessToken(
                userId = userId,
                issuedAt = now,
                expiresAt = now + 30.days,
                value = "valid-token",
                roles = setOf(UserRole.RESEARCHER),
            )

        runFilter("valid-token")

        val authorities =
            SecurityContextHolder
                .getContext()
                .authentication!!
                .authorities
                .map(GrantedAuthority::getAuthority)
        authorities shouldBe listOf("RESEARCHER")
    }

    @Test
    fun `a token with no roles authenticates with no authorities`() {
        val userId = createTestUserId()
        val now = Clock.System.now()
        every { authTokenService.decodeAccessToken("valid-token") } returns
            AccessToken(userId = userId, issuedAt = now, expiresAt = now + 30.days, value = "valid-token", roles = emptySet())

        runFilter("valid-token")

        SecurityContextHolder
            .getContext()
            .authentication!!
            .authorities
            .shouldBe(emptyList())
    }
}
