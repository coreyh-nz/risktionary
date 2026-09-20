import { AiFeedbackLabel } from "@/features/game/components/feedback/ai-feedback-label"
import { useGameFeedbackEnabled } from "@/features/game/stores/game-store-selectors"
import { useRoundFeedback } from "@/features/game/stores/selectors/feedback.selectors"

export const RoundFeedbackView = ({ roundNumber }: { roundNumber: number }) => {
  const enabled = useGameFeedbackEnabled()
  const feedback = useRoundFeedback(roundNumber)
  if (!enabled || !feedback) return null

  return (
    <div className="mx-auto w-full max-w-xl rounded-lg border border-primary/15 bg-muted/40 p-4">
      <AiFeedbackLabel />
      <p className="mt-2 text-sm leading-relaxed whitespace-pre-line text-foreground">
        {feedback.text}
      </p>
    </div>
  )
}
