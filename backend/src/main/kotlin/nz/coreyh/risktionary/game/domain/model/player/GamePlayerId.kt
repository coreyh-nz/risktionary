package nz.coreyh.risktionary.game.domain.model.player

import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class GamePlayerId(
    val value: UUID,
) {
    override fun toString(): String = value.toString()
}

fun UUID.toGamePlayerId(): GamePlayerId = GamePlayerId(this)

fun String.toGamePlayerIdOrNull(): GamePlayerId? = toUuidOrNull()?.let(::GamePlayerId)

fun createPlayerId(): GamePlayerId = GamePlayerId(UUID.randomUUID())
