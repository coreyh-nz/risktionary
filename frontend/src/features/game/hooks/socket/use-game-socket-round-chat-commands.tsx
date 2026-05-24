import { useWebSocket } from "@/providers/web-socket-provider"
import { useCallback } from "react"
import { ChatMessageCommand } from "../../types/round/commands"

export const useGameSocketRoundChatCommands = () => {
  const { send } = useWebSocket()

  const onChat = useCallback(
    (text: string) => {
      const message: ChatMessageCommand = { text }
      send("/app/game/chat", message)
    },
    [send]
  )

  return { onChat }
}
