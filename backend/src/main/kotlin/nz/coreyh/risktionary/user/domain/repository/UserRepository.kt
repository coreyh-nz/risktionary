package nz.coreyh.risktionary.user.domain.repository

import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.UserId

interface UserRepository {
    fun findById(id: UserId): User?

    fun findByEmail(email: String): User?

    fun create(
        email: String,
        firstName: String,
        lastName: String,
        displayName: String,
    ): User
}
