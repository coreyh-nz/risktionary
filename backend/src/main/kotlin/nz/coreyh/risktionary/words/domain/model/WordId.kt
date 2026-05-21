package nz.coreyh.risktionary.words.domain.model

import nz.coreyh.risktionary.shared.domain.model.Identifiable
import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class WordId(
    override val value: UUID,
) : Identifiable {
    override fun toString(): String = value.toString()
}

fun UUID.toWordId(): WordId = WordId(this)

fun String.toWordIdOrNull(): WordId? = toUuidOrNull()?.let(::WordId)

fun createWordId(): WordId = WordId(UUID.randomUUID())
