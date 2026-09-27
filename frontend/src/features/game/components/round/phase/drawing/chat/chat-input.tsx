import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useChatCommands } from "@/features/game/hooks/round/phase/drawing/use-chat-commands"
import { useGameIsDrawer } from "@/features/game/stores/game-store-selectors"
import { SendHorizonal } from "lucide-react"
import { SubmitEvent, useState } from "react"

export const ChatInput = () => {
  const { onChat } = useChatCommands()
  const isDrawing = useGameIsDrawer()
  const [draft, setDraft] = useState("")

  const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    const message = draft.trim()
    if (!message) return

    onChat(message)
    setDraft("")
  }

  return (
    <form onSubmit={handleSubmit} className="flex gap-2">
      <Input
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        placeholder={isDrawing ? "You are drawing..." : "Type a guess..."}
      />

      <Button type="submit" size="icon">
        <SendHorizonal />
      </Button>
    </form>
  )
}
