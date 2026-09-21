import { ScoreboardEntry } from "@/features/game/types/game/scoring"
import { CountdownState } from "@/types/time"
import { GameSessionPlayer } from "../../game"

export type Round = {
  number: number
  state: RoundState
}

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
      countdown?: CountdownState
      correctGuessCount: number
    }
  | {
      type: "DRAWING_REVIEW"
      countdown?: CountdownState
      word: string
    }
  | {
      type: "RANKING"
      countdown?: CountdownState
    }
  | {
      type: "RANKING_REVIEW"
      countdown?: CountdownState
    }
  | {
      type: "WORD_REVIEW"
      countdown?: CountdownState
    }
  | {
      type: "SAVING"
      countdown?: CountdownState
    }
  | {
      type: "SCORING"
      countdown?: CountdownState
      scoreboard?: ScoreboardEntry[]
    }
  | {
      type: "COMPLETED"
      countdown?: CountdownState
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
