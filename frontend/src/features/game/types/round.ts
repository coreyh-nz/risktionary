export type RoundPhaseType =
  | "DRAWING"
  | "DRAWING_REVIEW"
  | "GUESS_REVEAL"
  | "RANKING"
  | "RANKING_REVIEW"
  | "WORD_REVIEW"
  | "ROUND_SCORING"
  | "COMPLETED"

export type RoundStateType = "SELECTING_DRAWER" | "IN_PROGRESS" | "COMPLETED"

export type RoundState =
  | { type: "SELECTING_DRAWER" }
  | { type: "IN_PROGRESS"; drawerId: string; phase: RoundPhaseType }
  | { type: "COMPLETED" }
