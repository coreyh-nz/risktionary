import { RoundFeedbackView } from "@/features/game/components/feedback/round-feedback-view"
import { useGameRound } from "@/features/game/stores/selectors/round.selectors"

export const PhaseScoringScreen = () => {
  const round = useGameRound()
  return (
    <div className="flex flex-col gap-4">
      <div>Scoring</div>
      {round && <RoundFeedbackView roundNumber={round.number} />}
    </div>
  )
}
