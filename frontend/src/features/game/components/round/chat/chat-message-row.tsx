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
import { CheckCircle2, Clock, Trophy } from "lucide-react"

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

interface SystemMessageRowProps {
  icon: React.ReactNode
  text: string
  className: string
}

const SystemMessageRow = ({ icon, text, className }: SystemMessageRowProps) => (
  <div
    className={`mx-2 my-1 flex items-center gap-2 rounded-md px-3 py-1.5 ${className}`}
  >
    {icon}
    <span className="text-[12px] font-medium">{text}</span>
  </div>
)

const SystemChatMessageRow = ({ message }: { message: SystemMessage }) => {
  const isMe = useGameIsPlayerMe(
    message.kind === "PLAYER_GUESSED_CORRECTLY" ? message.playerId : ""
  )

  const kind = message.kind
  switch (kind) {
    case "PLAYER_GUESSED_CORRECTLY":
      return (
        <SystemMessageRow
          icon={
            <CheckCircle2 className="size-3.5 shrink-0 text-emerald-600 dark:text-emerald-400" />
          }
          text={`${isMe ? "You" : message.playerDisplayName} guessed correctly!`}
          className="bg-emerald-500/10 text-emerald-700 dark:text-emerald-300"
        />
      )
    case "DRAWING_ENDED_ALL_GUESSED":
      return (
        <SystemMessageRow
          icon={
            <Trophy className="size-3.5 shrink-0 text-amber-600 dark:text-amber-400" />
          }
          text={`Everyone guessed it! The word was "${message.word}"`}
          className="bg-amber-500/10 text-amber-700 dark:text-amber-300"
        />
      )
    case "DRAWING_ENDED_TIME_UP":
      return (
        <SystemMessageRow
          icon={
            <Clock className="size-3.5 shrink-0 text-blue-600 dark:text-blue-400" />
          }
          text={`Time's up! The word was "${message.word}"`}
          className="bg-blue-500/10 text-blue-700 dark:text-blue-300"
        />
      )
    default:
      assertNever(kind)
  }
}
