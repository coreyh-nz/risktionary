import { PhaseCompletedScreen } from "@/features/game/components/round/phase/completed/phase-completed-screen"
import { PhaseDrawingScreen } from "@/features/game/components/round/phase/drawing/phase-drawing-screen"
import { PhaseRiskRatingScreen } from "@/features/game/components/round/phase/rating/phase-risk-rating-screen"
import { PhaseScoringScreen } from "@/features/game/components/round/phase/scoring/phase-scoring-screen"
import { PhaseWordReviewScreen } from "@/features/game/components/round/phase/word-review/phase-word-review-screen"
import { RoundPhaseType } from "@/features/game/types/round/phase/round"
import { ComponentType } from "react"

export const PHASE_COMPONENTS: Record<RoundPhaseType, ComponentType> = {
  DRAWING: PhaseDrawingScreen,
  DRAWING_REVIEW: PhaseDrawingScreen,
  RANKING: PhaseRiskRatingScreen,
  RANKING_REVIEW: PhaseRiskRatingScreen,
  WORD_REVIEW: PhaseWordReviewScreen,
  SCORING: PhaseScoringScreen,
  COMPLETED: PhaseCompletedScreen,
}
