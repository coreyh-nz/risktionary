import { ScrollArea } from "@/components/ui/scroll-area"
import { useGameRoundChatMessages } from "@/features/game/stores/game-store-selectors"
import { useEffect, useRef } from "react"
import { ChatMessageRow } from "./chat-message-row"

export const ChatFeed = () => {
  const messages = useGameRoundChatMessages()
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  return (
    <ScrollArea className="h-full">
      <div className="flex flex-col gap-2 py-2 w-full max-w-full overflow-hidden">
        {messages.map((message, idx) => (
          <ChatMessageRow key={idx} message={message} />
        ))}
        <div ref={bottomRef} />
      </div>
    </ScrollArea>
  )
}
