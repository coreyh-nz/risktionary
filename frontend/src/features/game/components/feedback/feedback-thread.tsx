import { AiFeedbackLabel } from "@/features/game/components/feedback/ai-feedback-label"
import { useGameFeedbackEnabled } from "@/features/game/stores/game-store-selectors"
import {
  useFeedbackSeenAt,
  useMessageFeedback,
} from "@/features/game/stores/selectors/feedback.selectors"

interface FeedbackThreadProps {
  messageId: string
}

// Rendered under one of the current player's own messages only.
export const FeedbackThread = ({ messageId }: FeedbackThreadProps) => {
  const enabled = useGameFeedbackEnabled()
  const feedback = useMessageFeedback(messageId)
  const seenAt = useFeedbackSeenAt()

  if (!enabled || !feedback) return null

  return (
    <div className="mr-2 mb-1 ml-6 rounded-r-md border-l-2 border-primary/20 bg-muted/40 py-1 pr-2 pl-2.5">
      <div className="flex items-baseline gap-2">
        {feedback.receivedAt > seenAt && (
          <span
            aria-label="Unread"
            className="size-1.5 self-center rounded-full bg-primary"
          />
        )}
        <AiFeedbackLabel />
        <span className="text-[10px] text-muted-foreground/70 tabular-nums">
          {new Date(feedback.receivedAt).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
          })}
        </span>
      </div>
      <p className="text-[13px] leading-snug wrap-break-word text-foreground/90">
        {feedback.text}
      </p>
    </div>
  )
}
