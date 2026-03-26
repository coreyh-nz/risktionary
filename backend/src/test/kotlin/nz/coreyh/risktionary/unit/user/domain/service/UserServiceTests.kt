package nz.coreyh.risktionary.unit.user.domain.service

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nz.coreyh.risktionary.support.factory.user.createTestUser
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.repository.UserRepository
import nz.coreyh.risktionary.user.domain.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserServiceTests {
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    @Test
    fun `find by id returns user when repository returns user`() {
        val user = createTestUser()
        every { userRepository.findById(user.id) } returns user

        val result = userService.findById(user.id)

        result shouldBe user
        verify(exactly = 1) { userRepository.findById(user.id) }
    }

    @Test
    fun `find by id returns null when repository returns null`() {
        val userId = createTestUserId()
        every { userRepository.findById(userId) } returns null

        val result = userService.findById(userId)

        result.shouldBeNull()
        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `find by email lowercases email and returns user when repository returns user`() {
        val email = "JANE.doe@gmail.com"
        val normalizedEmail = email.lowercase()
        val user = createTestUser(email = normalizedEmail)
        every { userRepository.findByEmail(normalizedEmail) } returns user

        val result = userService.findByEmail(email)

        result shouldBe user
        verify(exactly = 1) { userRepository.findByEmail(normalizedEmail) }
    }

    @Test
    fun `find by email lowercases email returns null when repository returns null`() {
        val email = "JANE.doe@gmail.com"
        val normalizedEmail = email.lowercase()
        every { userRepository.findByEmail(normalizedEmail) } returns null

        val result = userService.findByEmail(normalizedEmail)

        result.shouldBeNull()
        verify(exactly = 1) { userRepository.findByEmail(normalizedEmail) }
    }

    @Test
    fun `create lowercases email and delegates to repository`() {
        val email = "JANE.doe@gmail.com"
        val normalizedEmail = email.lowercase()
        val user = createTestUser(email = normalizedEmail)
        every {
            userRepository.create(
                email = normalizedEmail,
                firstName = user.firstName,
                lastName = user.lastName,
                displayName = user.displayName,
            )
        } returns user

        val result =
            userService.create(
                email = email,
                firstName = user.firstName,
                lastName = user.lastName,
                displayName = user.displayName,
            )

        result shouldBe user
        verify(exactly = 1) {
            userRepository.create(
                email = normalizedEmail,
                firstName = user.firstName,
                lastName = user.lastName,
                displayName = user.displayName,
            )
        }
    }
}
