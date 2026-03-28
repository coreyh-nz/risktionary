package nz.coreyh.risktionary.integration.user.web.controller

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestUserCreator
import nz.coreyh.risktionary.support.extensions.andBody
import nz.coreyh.risktionary.support.extensions.auth
import nz.coreyh.risktionary.user.web.dto.UserDetailsDto
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

@IntegrationTest
class UserControllerIntegrationTests(
    private val mockMvc: MockMvc,
    private val testUserCreator: TestUserCreator,
    private val objectMapper: ObjectMapper,
) {
    @Test
    fun `given authenticated user, when getting me, then returns ok`() {
        val user = testUserCreator.createTestUser()

        mockMvc
            .get(Routes.V1.User.ME) {
                auth(user)
            }.andExpect {
                status {
                    isOk()
                }
            }.andBody {
                val dto = objectMapper.readValue<UserDetailsDto>(it)
                dto.id shouldBe user.id
                dto.email shouldBe user.email
                dto.firstName shouldBe user.firstName
                dto.lastName shouldBe user.lastName
            }
    }

    @Test
    fun `given no authentication, when getting me, then returns unauthorized`() {
        mockMvc
            .get(Routes.V1.User.ME)
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
