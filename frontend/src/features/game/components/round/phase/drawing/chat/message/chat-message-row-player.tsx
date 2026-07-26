import { useGameSession } from "@/features/game/stores/game-store-selectors"
import { PlayerMessage } from "@/features/game/types/round/phase/drawing/chat"

interface ChatMessageRowPlayerProps {
  message: PlayerMessage
}

export const ChatMessageRowPlayer = ({
  message,
}: ChatMessageRowPlayerProps) => {
  const session = useGameSession()
  const isMe =
    session?.role === "player" && session.playerId === message.player.id
  return (
    <div className="group flex items-baseline gap-1.5 px-3.5 py-0.5 hover:bg-muted/50">
      <span
        className={`shrink-0 text-xs font-medium ${isMe ? "text-primary" : "text-muted-foreground"}`}
      >
        {message.player.displayName}:
      </span>
      <span className="break-all text-foreground">{message.text}</span>
    </div>
  )
}
