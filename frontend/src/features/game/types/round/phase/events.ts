import { WordHint } from "@/features/game/types/round/phase/round"
import { GameSessionPlayer } from "../../game"
import { ChatMessage } from "@/features/game/types/round/phase/drawing/chat"

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

export type RoundEvent =
  | RoundStateEvent
  | RoundAssignedDrawerEvent
  | RoundAssignedGuesserEvent
  | RoundChatMessageEvent
  | RoundCorrectGuessEvent
  | RoundCorrectGuessesUpdatedEvent
