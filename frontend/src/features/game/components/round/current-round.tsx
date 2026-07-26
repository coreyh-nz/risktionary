import { useGameRoundState } from "@/features/game/stores/game-store-selectors"
import { ROUND_COMPONENTS } from "./round-registry"

export const CurrentRound = () => {
  const roundState = useGameRoundState()
  if (!roundState) return null

  const RoundComponent = ROUND_COMPONENTS[roundState.type]
  return <RoundComponent />
}
