import { PHASE_COMPONENTS } from "@/features/game/components/round/phase/phase-registry"
import { useGameRound } from "@/features/game/stores/selectors/round.selectors"
import { RoundPhaseStatusBar } from "../round-phase-status-bar"

export const CurrentPhase = () => {
  const round = useGameRound()
  if (!round || round.state.type != "IN_PROGRESS") return null

  const PhaseComponent = PHASE_COMPONENTS[round.state.phase.type]

  return (
    <div className="flex min-h-0 min-w-0 flex-1 flex-col">
      <RoundPhaseStatusBar />
      <div className="flex min-h-0 flex-1 flex-col gap-3 p-4">
        <PhaseComponent />
      </div>
    </div>
  )
}
