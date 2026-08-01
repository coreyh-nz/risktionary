import { PHASE_COMPONENTS } from "@/features/game/components/round/phase/phase-registry"
import { useGameRoundState } from "@/features/game/stores/game-store-selectors"

export const CurrentPhase = () => {
  const roundState = useGameRoundState()
  if (!roundState || roundState.type != "IN_PROGRESS") return null

  const roundPhase = roundState.phase
  if (!roundPhase) return null

  const PhaseComponent = PHASE_COMPONENTS[roundPhase.type]
  return <PhaseComponent />
}
