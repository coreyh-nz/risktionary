import { useShallow } from "zustand/shallow"
import { useGameStore } from "../game-store"

export const useGameRound = () => useGameStore(useShallow((s) => s.round))

export const useGameRoundRiskRatingCounts = () =>
  useGameStore(useShallow((s) => s.roundRiskRatingCounts))

export const useGameRoundSubmittedRiskRating = () =>
  useGameStore((s) => s.roundSubmittedRiskRating)

export const useGameRoundSetSubmittedRiskRating = () =>
  useGameStore((s) => s.setRoundSubmittedRiskRating)
