import { GameSessionPlayer } from "../../game"

export type RoundState =
  | { type: "SELECTING_DRAWER" }
  | {
      type: "IN_PROGRESS"
      drawer: GameSessionPlayer
      phase: RoundPhase
    }
  | { type: "COMPLETED" }

export type RoundStateType = RoundState["type"]

export type RoundPhase =
  | {
      type: "DRAWING"
      correctGuessCount: number
    }
  | {
      type: "DRAWING_REVIEW"
      word: string
    }
  | {
      type: "RANKING"
    }
  | {
      type: "RANKING_REVIEW"
    }
  | {
      type: "WORD_REVIEW"
    }
  | {
      type: "SCORING"
    }
  | {
      type: "COMPLETED"
    }

export type RoundPhaseType = RoundPhase["type"]

export type RoundRole =
  | { type: "DRAWER"; word: string }
  | { type: "GUESSER"; hint: WordHint; correctGuessWord?: string }

export type CharacterHint =
  | { type: "HIDDEN" }
  | { type: "SPACE" }
  | { type: "REVEALED"; character: string }

export interface WordHint {
  characters: CharacterHint[]
}
