package nz.coreyh.risktionary.user.domain.service

import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.user.domain.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findById(id: UserId): User? = userRepository.findById(id)

    fun findByEmail(email: String): User? = userRepository.findByEmail(email.lowercase())

    fun create(
        email: String,
        firstName: String,
        lastName: String,
        displayName: String,
    ): User = userRepository.create(email = email.lowercase(), firstName = firstName, lastName = lastName, displayName = displayName)
}
