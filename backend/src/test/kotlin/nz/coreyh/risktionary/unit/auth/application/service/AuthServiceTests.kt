package nz.coreyh.risktionary.unit.auth.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.auth.application.service.AuthService
import nz.coreyh.risktionary.auth.application.service.AuthTokenService
import nz.coreyh.risktionary.auth.application.service.OAuthAccountService
import nz.coreyh.risktionary.support.factory.auth.createTestAccessToken
import nz.coreyh.risktionary.support.factory.auth.createTestOAuthAccount
import nz.coreyh.risktionary.support.factory.auth.createTestOAuthUserInfo
import nz.coreyh.risktionary.support.factory.user.createTestUser
import nz.coreyh.risktionary.support.persistence.TestTransactional
import nz.coreyh.risktionary.user.domain.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthServiceTests {
    private lateinit var userService: UserService
    private lateinit var oAuthAccountService: OAuthAccountService
    private lateinit var authTokenService: AuthTokenService
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        userService = mockk()
        oAuthAccountService = mockk()
        authTokenService = mockk()
        authService = AuthService(userService, oAuthAccountService, authTokenService, TestTransactional())
    }

    @Test
    fun `authenticate oauth user returns token when user exists and oauth account is linked`() {
        val user = createTestUser()
        val oauthUserInfo = createTestOAuthUserInfo(user = user)
        val linkedAccount = createTestOAuthAccount(user = user, oauthUserInfo = oauthUserInfo)
        val token = createTestAccessToken(userId = user.id)
        every {
            oAuthAccountService.findByProviderIdentity(oauthUserInfo.provider, oauthUserInfo.providerUserId)
        } returns linkedAccount
        every { userService.findById(user.id) } returns user
        every { authTokenService.generateAccessToken(user) } returns token

        val result = authService.authenticateOAuthUser(oauthUserInfo)

        result shouldBe token
        verify(exactly = 1) {
            oAuthAccountService.findByProviderIdentity(
                oauthUserInfo.provider,
                oauthUserInfo.providerUserId,
            )
        }
        verify(exactly = 1) { userService.findById(user.id) }
        verify(exactly = 1) { authTokenService.generateAccessToken(user) }
        verify(exactly = 0) { userService.findByEmail(any()) }
        verify(exactly = 0) { userService.create(any(), any(), any(), any()) }
    }

    @Test
    fun `authenticate oauth user links oauth account and returns token when user exists and oauth account is not linked`() {
        val user = createTestUser()
        val oauthUserInfo = createTestOAuthUserInfo(user = user)
        val linkedAccount = createTestOAuthAccount(user = user, oauthUserInfo = oauthUserInfo)
        val token = createTestAccessToken(userId = user.id)
        every {
            oAuthAccountService.findByProviderIdentity(oauthUserInfo.provider, oauthUserInfo.providerUserId)
        } returns null
        every {
            oAuthAccountService.linkAccount(
                userId = user.id,
                provider = oauthUserInfo.provider,
                providerUserId = oauthUserInfo.providerUserId,
                email = oauthUserInfo.email,
            )
        } returns linkedAccount
        every { userService.findByEmail(oauthUserInfo.email) } returns user
        every { authTokenService.generateAccessToken(user) } returns token

        val result = authService.authenticateOAuthUser(oauthUserInfo)

        result shouldBe token
        verify(exactly = 1) {
            oAuthAccountService.findByProviderIdentity(
                oauthUserInfo.provider,
                oauthUserInfo.providerUserId,
            )
        }
        verify(exactly = 1) { userService.findByEmail(user.email) }
        verify(exactly = 1) {
            oAuthAccountService.linkAccount(
                userId = user.id,
                provider = oauthUserInfo.provider,
                providerUserId = oauthUserInfo.providerUserId,
                email = oauthUserInfo.email,
            )
        }
    }

    @Test
    fun `authenticate oauth user creates user, links oauth account, and returns token when user does not exist`() {
        val createdUser = createTestUser()
        val oauthUserInfo = createTestOAuthUserInfo(user = createdUser)
        val linkedAccount = createTestOAuthAccount(user = createdUser, oauthUserInfo = oauthUserInfo)
        val token = createTestAccessToken(userId = createdUser.id)
        every {
            oAuthAccountService.findByProviderIdentity(oauthUserInfo.provider, oauthUserInfo.providerUserId)
        } returns null
        every {
            oAuthAccountService.linkAccount(
                userId = createdUser.id,
                provider = oauthUserInfo.provider,
                providerUserId = oauthUserInfo.providerUserId,
                email = oauthUserInfo.email,
            )
        } returns linkedAccount
        every { userService.findByEmail(oauthUserInfo.email) } returns null
        every {
            userService.create(
                email = oauthUserInfo.email,
                firstName = oauthUserInfo.firstName,
                lastName = oauthUserInfo.lastName,
                displayName = oauthUserInfo.displayName,
            )
        } returns createdUser
        every { authTokenService.generateAccessToken(createdUser) } returns token

        val result = authService.authenticateOAuthUser(oauthUserInfo)

        result shouldBe token
        verify(exactly = 1) {
            oAuthAccountService.findByProviderIdentity(
                oauthUserInfo.provider,
                oauthUserInfo.providerUserId,
            )
        }
        verify(exactly = 1) { userService.findByEmail(createdUser.email) }
        verify(exactly = 1) {
            userService.create(
                email = oauthUserInfo.email,
                firstName = oauthUserInfo.firstName,
                lastName = oauthUserInfo.lastName,
                displayName = oauthUserInfo.displayName,
            )
        }
        verify(exactly = 1) {
            oAuthAccountService.linkAccount(
                userId = createdUser.id,
                provider = oauthUserInfo.provider,
                providerUserId = oauthUserInfo.providerUserId,
                email = oauthUserInfo.email,
            )
        }
    }
}
