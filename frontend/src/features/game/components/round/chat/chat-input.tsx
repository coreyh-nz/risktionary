import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useGameSocketRoundChatCommands } from "@/features/game/hooks/socket/use-game-socket-round-chat-commands"
import { useGameIsDrawer } from "@/features/game/stores/game-store-selectors"
import { SendHorizonal } from "lucide-react"
import { useState } from "react"

export const ChatInput = () => {
  const { onChat } = useGameSocketRoundChatCommands()
  const isDrawing = useGameIsDrawer()
  const [draft, setDraft] = useState("")

  const handleSend = () => {
    onChat(draft)
    setDraft("")
  }

  return (
    <div className="flex gap-2">
      <Input
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && handleSend()}
        placeholder={isDrawing ? "You are drawing..." : "Type a guess..."}
      />
      <Button size="icon">
        <SendHorizonal />
      </Button>
    </div>
  )
}
