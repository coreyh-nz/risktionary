package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.feedback.domain.model.FeedbackId
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessageId
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType

/**
 * Feedback for a player, sent to that player only.
 *
 * [messageId] is the chat message the feedback replies to (the message
 * carrying the guess it was generated from) and is set for instant feedback.
 * It is null for delayed feedback, which is about the round as a whole.
 */
data class RoundFeedbackEvent(
    val feedbackId: FeedbackId,
    val messageId: ChatMessageId?,
    val timing: FeedbackTimingCondition,
    val text: String,
) : RoundEvent(RoundEventType.FEEDBACK)
