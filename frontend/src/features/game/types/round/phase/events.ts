import { ChatMessage } from "@/features/game/types/round/phase/drawing/chat"
import { RiskRatingCount } from "@/features/game/types/round/phase/risk/risk"
import { WordHint } from "@/features/game/types/round/phase/round"
import { TimerView } from "@/types/time"
import { GameSessionPlayer } from "../../game"

export type RoundView = {
  number: number
  state: RoundStateView
}

export type RoundStateView =
  | {
      type: "SELECTING_DRAWER"
    }
  | {
      type: "IN_PROGRESS"
      drawer: GameSessionPlayer
      phase: RoundPhaseStateView
    }
  | {
      type: "COMPLETED"
    }

export type RoundPhaseStateView =
  | {
      type: "DRAWING"
      timer?: TimerView
      correctGuessCount: number
    }
  | {
      type: "DRAWING_REVIEW"
      timer?: TimerView
      word: string
    }
  | {
      type: "RANKING"
      timer?: TimerView
    }
  | {
      type: "RANKING_REVIEW"
      timer?: TimerView
    }
  | {
      type: "WORD_REVIEW"
      timer?: TimerView
    }
  | {
      type: "SCORING"
      timer?: TimerView
    }
  | {
      type: "COMPLETED"
      timer?: TimerView
    }

export interface RoundStateEvent {
  type: "STATE"
  state: RoundStateView
}

export interface RoundPhaseStateEvent {
  type: "PHASE_STATE"
  phase: RoundPhaseStateView
}

export interface RoundAssignedDrawerEvent {
  type: "ASSIGNED_DRAWER"
  word: string
}

export interface RoundAssignedGuesserEvent {
  type: "ASSIGNED_GUESSER"
  hint: WordHint
}

export interface RoundChatMessageEvent {
  type: "CHAT_MESSAGE"
  message: ChatMessage
}

export interface RoundCorrectGuessEvent {
  type: "CORRECT_GUESS"
  word: string
}

export interface RoundCorrectGuessesUpdatedEvent {
  type: "CORRECT_GUESSES_COUNT"
  correctGuesses: number
}

export interface RoundRiskRatingsUpdatedEvent {
  type: "RISK_RATINGS_UPDATED"
  counts: RiskRatingCount[]
}

export type RoundEvent =
  | RoundStateEvent
  | RoundPhaseStateEvent
  | RoundAssignedDrawerEvent
  | RoundAssignedGuesserEvent
  | RoundChatMessageEvent
  | RoundCorrectGuessEvent
  | RoundCorrectGuessesUpdatedEvent
  | RoundRiskRatingsUpdatedEvent
