import { RoundNotActiveError } from "../../errors/round"
import { useGameRound } from "../../stores/selectors/round.selectors"
import { ROUND_COMPONENTS } from "./round-registry"

export const CurrentRound = () => {
  const round = useGameRound()
  if (!round) throw new RoundNotActiveError()

  const RoundComponent = ROUND_COMPONENTS[round.state.type]
  return <RoundComponent />
}
