import { ChatMessageRowPlayer } from "@/features/game/components/round/chat/message/chat-message-row-player"
import { SystemChatMessageRow } from "@/features/game/components/round/chat/message/chat-message-row-system"
import { ChatMessage } from "@/features/game/types/round/chat"
import { assertNever } from "@/lib/utils"

interface ChatMessageProps {
  message: ChatMessage
}

export const ChatMessageRow = ({ message }: ChatMessageProps) => {
  const type = message.type
  switch (type) {
    case "PLAYER":
      return <ChatMessageRowPlayer message={message} />
    case "SYSTEM":
      return <SystemChatMessageRow message={message} />
    default:
      assertNever(type)
  }
}
