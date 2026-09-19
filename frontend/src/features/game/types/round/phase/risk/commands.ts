import {
  RiskLikelihood,
  RiskSeverity,
} from "@/features/game/types/round/phase/risk/risk"

export interface RiskRatingCommand {
  likelihood: RiskLikelihood
  severity: RiskSeverity
}
