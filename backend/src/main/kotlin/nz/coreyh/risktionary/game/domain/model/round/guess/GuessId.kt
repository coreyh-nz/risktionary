package nz.coreyh.risktionary.game.domain.model.round.guess

import nz.coreyh.risktionary.shared.domain.model.Identifiable
import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class GuessId(
    override val value: UUID,
) : Identifiable {
    override fun toString(): String = value.toString()
}

fun UUID.toGuessId(): GuessId = GuessId(this)

fun String.toGuessIdOrNull(): GuessId? = toUuidOrNull()?.let(::GuessId)

fun createGuessId(): GuessId = GuessId(UUID.randomUUID())
