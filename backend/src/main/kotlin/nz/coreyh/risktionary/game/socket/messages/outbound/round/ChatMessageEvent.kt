package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType

data class ChatMessageEvent(
    val message: ChatMessage,
) : RoundEvent(RoundEventType.CHAT_MESSAGE)
