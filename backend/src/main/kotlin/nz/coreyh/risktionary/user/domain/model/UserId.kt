package nz.coreyh.risktionary.user.domain.model

import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class UserId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()
}

fun UUID.toUserId(): UserId = UserId(this)

fun String.toUserIdOrNull(): UserId? = toUuidOrNull()?.let(::UserId)
