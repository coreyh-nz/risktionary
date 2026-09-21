import { RoundFeedbackView } from "@/features/game/components/feedback/round-feedback-view"
import { RoundScoreboard } from "@/features/game/components/scoring/round-scoreboard"
import { useGameRound } from "@/features/game/stores/selectors/round.selectors"

export const PhaseScoringScreen = () => {
  const round = useGameRound()
  if (!round || round.state.type !== "IN_PROGRESS") return null

  const phase = round.state.phase
  const scoreboard = phase.type === "SCORING" ? phase.scoreboard : undefined

  return (
    <div className="flex min-h-0 flex-1 flex-col items-center gap-4 overflow-y-auto">
      <div className="flex w-full max-w-3xl flex-col gap-4">
        <h2 className="text-center text-xl font-bold">
          Round {round.number} results
        </h2>
        <RoundScoreboard scoreboard={scoreboard} />
        <RoundFeedbackView roundNumber={round.number} />
      </div>
    </div>
  )
}
