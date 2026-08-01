import { PhaseCompletedScreen } from "@/features/game/components/round/phase/completed/phase-completed-screen"
import { PhaseDrawingScreen } from "@/features/game/components/round/phase/drawing/phase-drawing-screen"
import { PhaseRankingScreen } from "@/features/game/components/round/phase/ranking/phase-ranking-screen"
import { PhaseScoringScreen } from "@/features/game/components/round/phase/scoring/phase-scoring-screen"
import { PhaseWordReviewScreen } from "@/features/game/components/round/phase/word-review/phase-word-review-screen"
import { RoundPhaseType } from "@/features/game/types/round/phase/round"
import { ComponentType } from "react"

export const PHASE_COMPONENTS: Record<RoundPhaseType, ComponentType> = {
  DRAWING: PhaseDrawingScreen,
  DRAWING_REVIEW: PhaseDrawingScreen,
  RANKING: PhaseRankingScreen,
  RANKING_REVIEW: PhaseRankingScreen,
  WORD_REVIEW: PhaseWordReviewScreen,
  SCORING: PhaseScoringScreen,
  COMPLETED: PhaseCompletedScreen,
}
