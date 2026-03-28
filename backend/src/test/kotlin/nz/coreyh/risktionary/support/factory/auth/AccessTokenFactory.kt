package nz.coreyh.risktionary.support.factory.auth

import nz.coreyh.risktionary.auth.domain.model.AccessToken
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.model.UserId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

fun createTestAccessToken(
    userId: UserId = createTestUserId(),
    issuedAt: Instant = Clock.System.now(),
    expiresAt: Instant = issuedAt + 30.days,
    value: String = "$userId-access-token",
): AccessToken =
    AccessToken(
        userId = userId,
        issuedAt = issuedAt,
        expiresAt = expiresAt,
        value = value,
    )
