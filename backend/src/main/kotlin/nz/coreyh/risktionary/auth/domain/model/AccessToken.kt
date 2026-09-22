package nz.coreyh.risktionary.auth.domain.model

import nz.coreyh.risktionary.user.domain.model.UserId
import kotlin.time.Instant
import nz.coreyh.risktionary.user.domain.model.UserRole

data class AccessToken(
    val userId: UserId,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val value: String,
    val roles: Set<UserRole> = emptySet(),
)
