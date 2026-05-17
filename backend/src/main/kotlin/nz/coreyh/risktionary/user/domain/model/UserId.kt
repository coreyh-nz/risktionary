package nz.coreyh.risktionary.user.domain.model

import nz.coreyh.risktionary.shared.domain.model.Identifiable
import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class UserId(
    override val value: UUID,
) : Identifiable {
    override fun toString(): String = value.toString()
}

fun UUID.toUserId(): UserId = UserId(this)

fun String.toUserIdOrNull(): UserId? = toUuidOrNull()?.let(::UserId)
