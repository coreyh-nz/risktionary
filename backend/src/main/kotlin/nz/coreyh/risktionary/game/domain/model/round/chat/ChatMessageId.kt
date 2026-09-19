package nz.coreyh.risktionary.game.domain.model.round.chat

import nz.coreyh.risktionary.shared.domain.model.Identifiable
import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class ChatMessageId(
    override val value: UUID,
) : Identifiable {
    override fun toString(): String = value.toString()
}

fun UUID.toChatMessageId(): ChatMessageId = ChatMessageId(this)

fun String.toChatMessageIdOrNull(): ChatMessageId? = toUuidOrNull()?.let(::ChatMessageId)

fun createChatMessageId(): ChatMessageId = ChatMessageId(UUID.randomUUID())
