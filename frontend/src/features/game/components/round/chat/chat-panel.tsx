import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { useGameIsHost } from "@/features/game/stores/game-store-selectors"
import { ComponentProps } from "react"
import { CorrectGuessesCount } from "../correct-guesses-count"
import { ChatFeed } from "./chat-feed"
import { ChatInput } from "./chat-input"

export const ChatPanel = ({ ...props }: ComponentProps<typeof Card>) => {
  const isHost = useGameIsHost()

  return (
    <Card className="flex flex-col" {...props}>
      <CardHeader>
        <CardTitle> Chat &amp; Guesses</CardTitle>
        <span className="text-xs text-muted-foreground tabular-nums">
          <CorrectGuessesCount /> players guessed correctly
        </span>
      </CardHeader>
      <CardContent className="flex-1 min-h-0 overflow-hidden px-1">
        <ChatFeed />
      </CardContent>

      {!isHost && (
        <CardFooter>
          <ChatInput />
        </CardFooter>
      )}
    </Card>
  )
}
