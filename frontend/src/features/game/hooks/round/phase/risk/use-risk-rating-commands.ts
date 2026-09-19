import { RiskRatingCommand } from "@/features/game/types/round/phase/risk/commands"
import {
  RiskLikelihood,
  RiskSeverity,
} from "@/features/game/types/round/phase/risk/risk"
import { useWebSocket } from "@/providers/web-socket-provider"
import { useCallback } from "react"

export const useRiskRatingCommands = () => {
  const { send } = useWebSocket()

  const onRiskRating = useCallback(
    (likelihood: RiskLikelihood, severity: RiskSeverity) => {
      const message: RiskRatingCommand = { likelihood, severity }
      send("/app/game/risk-rating", message)
    },
    [send]
  )

  return { onRiskRating }
}
