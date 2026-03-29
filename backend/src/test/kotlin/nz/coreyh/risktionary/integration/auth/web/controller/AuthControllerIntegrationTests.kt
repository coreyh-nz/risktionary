package nz.coreyh.risktionary.integration.auth.web.controller

import io.kotest.matchers.nulls.shouldNotBeNull
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestUserCreator
import nz.coreyh.risktionary.support.extensions.auth
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
@Transactional
class AuthControllerIntegrationTests(
    private val authConfiguration: AuthConfiguration,
    private val mockMvc: MockMvc,
    private val testUserCreator: TestUserCreator,
) {
    @Test
    fun `given authenticated user, when logging out, then returns ok and clears cookie`() {
        val user = testUserCreator.createTestUser()

        mockMvc
            .get(Routes.V1.Auth.LOGOUT) {
                auth(user)
            }.andExpect {
                status { isOk() }
                cookie {
                    exists(authConfiguration.cookie.accessTokenName)
                    path(authConfiguration.cookie.accessTokenName, authConfiguration.cookie.path)
                    secure(authConfiguration.cookie.accessTokenName, authConfiguration.cookie.secure)
                    httpOnly(authConfiguration.cookie.accessTokenName, true)

                    // same site can be omitted, but we should always have it set
                    val expectedSameSiteAttribute = authConfiguration.cookie.sameSite.attributeValue()
                    expectedSameSiteAttribute.shouldNotBeNull()
                    sameSite(authConfiguration.cookie.accessTokenName, expectedSameSiteAttribute)
                }
            }
    }

    @Test
    fun `given no authentication, when logging out, then returns unauthorized`() {
        mockMvc
            .get(Routes.V1.Auth.LOGOUT)
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
