package nz.coreyh.risktionary.feedback.domain.model

import nz.coreyh.risktionary.shared.domain.model.Identifiable
import nz.coreyh.risktionary.shared.extensions.toUuidOrNull
import java.util.UUID

@JvmInline
value class FeedbackId(
    override val value: UUID,
) : Identifiable {
    override fun toString(): String = value.toString()
}

fun UUID.toFeedbackId(): FeedbackId = FeedbackId(this)

fun String.toFeedbackIdOrNull(): FeedbackId? = toUuidOrNull()?.let(::FeedbackId)

fun createFeedbackId(): FeedbackId = FeedbackId(UUID.randomUUID())
