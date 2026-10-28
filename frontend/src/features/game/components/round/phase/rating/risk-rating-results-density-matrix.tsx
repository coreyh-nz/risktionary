import { RiskMatrix } from "@/features/game/components/round/phase/rating/matrix/risk-matrix"
import {
  DensityTier,
  RiskMatrixDensityCell,
} from "@/features/game/components/round/phase/rating/matrix/risk-matrix-density-cell"
import { useGameRoundRiskRatingCounts } from "@/features/game/stores/selectors/round.selectors"
import {
  RiskLikelihood,
  RiskSeverity,
} from "@/features/game/types/round/phase/risk/risk"
import { useMemo } from "react"

export const RiskRatingResultsDensityMatrix = () => {
  const riskRatings = useGameRoundRiskRatingCounts()
  const riskRatingLookup = useMemo(
    () =>
      riskRatings === null // if null, no player has voted or this player has not voted
        ? new Map()
        : new Map(
            riskRatings.map((r) => [
              getRiskRatingKey(r.likelihood, r.severity),
              r.count,
            ])
          ),
    [riskRatings]
  )
  const maxCount = useMemo(
    () =>
      riskRatings === null
        ? 0
        : Math.max(...riskRatings.map((r) => r.count), 0),
    [riskRatings]
  )

  return (
    <RiskMatrix
      renderCell={(likelihood, severity) => {
        const key = getRiskRatingKey(likelihood, severity)
        const count = riskRatingLookup.get(key) ?? 0
        const tier = getDensityTier(count, maxCount)
        return (
          <RiskMatrixDensityCell
            key={key}
            count={riskRatingLookup.get(key) ?? 0}
            tier={tier}
          />
        )
      }}
    />
  )
}

const getRiskRatingKey = (likelihood: RiskLikelihood, severity: RiskSeverity) =>
  `${likelihood}:${severity}`

const getDensityTier = (count: number, maxCount: number): DensityTier => {
  if (count === 0) return "none"
  if (count === maxCount) return "max"

  const ratio = count / maxCount
  if (ratio >= 0.75) return "high"
  if (ratio >= 0.5) return "medium"

  return "low"
}
