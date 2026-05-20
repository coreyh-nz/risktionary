package nz.coreyh.risktionary.game.domain.model.round

import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class RoundId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()
}

fun UUID.toRoundId(): RoundId = RoundId(this)

fun String.toRoundIdOrNull(): RoundId? = toUuidOrNull()?.let(::RoundId)

fun createRoundId(): RoundId = RoundId(UUID.randomUUID())
