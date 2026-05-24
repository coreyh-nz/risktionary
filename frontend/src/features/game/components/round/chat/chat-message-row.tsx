import {
  useGameIsPlayerMe,
  useGameSession,
} from "@/features/game/stores/game-store-selectors"
import {
  ChatMessage,
  PlayerMessage,
  SystemMessage,
} from "@/features/game/types/round/chat"
import { assertNever } from "@/lib/utils"
import { CheckCircle2 } from "lucide-react"

interface ChatMessageProps {
  message: ChatMessage
}

export const ChatMessageRow = ({ message }: ChatMessageProps) => {
  const type = message.type
  switch (type) {
    case "PLAYER":
      return <PlayerChatMessageRow message={message} />
    case "SYSTEM":
      return <SystemChatMessageRow message={message} />
    default:
      assertNever(type)
  }
}

const PlayerChatMessageRow = ({ message }: { message: PlayerMessage }) => {
  const session = useGameSession()
  const isMe =
    session?.role === "player" && session.playerId === message.playerId

  return (
    <div className="group flex items-baseline gap-1.5 px-3.5 py-0.5 hover:bg-muted/50">
      <span
        className={`shrink-0 text-xs font-medium ${isMe ? "text-primary" : "text-muted-foreground"}`}
      >
        {message.playerDisplayName}:
      </span>
      <span className="break-all text-foreground">{message.text}</span>
    </div>
  )
}

const SystemChatMessageRow = ({ message }: { message: SystemMessage }) => {
  const isMe = useGameIsPlayerMe(message.playerId)

  return (
    <div className="mx-2 my-1 flex items-center gap-2 rounded-md bg-emerald-500/10 px-3 py-1.5">
      <CheckCircle2 className="size-3.5 shrink-0 text-emerald-600 dark:text-emerald-400" />
      <span className="text-[12px] font-medium text-emerald-700 dark:text-emerald-300">
        {isMe ? "You" : message.playerDisplayName} guessed correctly!
      </span>
    </div>
  )
}
