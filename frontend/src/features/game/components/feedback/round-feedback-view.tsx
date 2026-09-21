import { AiFeedbackLabel } from "@/features/game/components/feedback/ai-feedback-label"
import { buildFeedbackGroups } from "@/features/game/components/feedback/feedback-entries"
import { useGameFeedbackEnabled } from "@/features/game/stores/game-store-selectors"
import { useFeedbackStore } from "@/features/game/stores/selectors/feedback.selectors"
import { useMemo } from "react"

// Everything the player received for the round, shown in full. Renders
// nothing if there is no feedback for the round.
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
    <div className="mx-auto flex w-full max-w-xl min-w-0 flex-col gap-3">
      {entries.map((entry) => (
        <div
          key={entry.id}
          className="rounded-lg border border-primary/15 bg-muted/40 p-4"
        >
          <div className="mb-1 text-xs font-medium wrap-break-word text-muted-foreground">
            {entry.title}
          </div>
          <AiFeedbackLabel />
          <p className="mt-1 text-sm leading-relaxed whitespace-pre-line text-foreground">
            {entry.text}
          </p>
        </div>
      ))}
    </div>
  )
}
