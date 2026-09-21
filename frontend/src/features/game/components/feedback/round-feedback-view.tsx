import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { buildFeedbackGroups } from "@/features/game/components/feedback/feedback-entries"
import { useGameFeedbackEnabled } from "@/features/game/stores/game-store-selectors"
import { useFeedbackStore } from "@/features/game/stores/selectors/feedback.selectors"
import { Sparkles } from "lucide-react"
import { useMemo } from "react"

export const RoundFeedbackView = ({ roundNumber }: { roundNumber: number }) => {
  const enabled = useGameFeedbackEnabled()
  const { byMessage, byRound, guesses } = useFeedbackStore()

  const entries = useMemo(
    () =>
      buildFeedbackGroups(byMessage, byRound, guesses).find(
        (g) => g.round === roundNumber
      )?.entries ?? [],
    [byMessage, byRound, guesses, roundNumber]
  )

  if (!enabled || entries.length === 0) return null

  return (
    <Card className="mx-auto w-full max-w-xl min-w-0 bg-primary/5 ring-primary/30">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-base">
          <Sparkles className="size-4 text-primary" />
          AI feedback
        </CardTitle>
        <CardDescription>Feedback for you from this round</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {entries.map((entry) => (
          <div key={entry.id} className="border-l-2 border-primary/30 pl-3">
            <p className="mb-0.5 text-xs font-medium wrap-break-word text-muted-foreground">
              {entry.title}
            </p>
            <p className="text-sm leading-relaxed whitespace-pre-line text-foreground">
              {entry.text}
            </p>
          </div>
        ))}
      </CardContent>
    </Card>
  )
}
