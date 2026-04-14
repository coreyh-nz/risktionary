package nz.coreyh.risktionary.game.domain.model

import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class GameId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()
}

fun UUID.toGameId(): GameId = GameId(this)

fun String.toGameIdOrNull(): GameId? = toUuidOrNull()?.let(::GameId)

fun createGameId(): GameId = GameId(UUID.randomUUID())
