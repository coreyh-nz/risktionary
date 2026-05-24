import { RoundState, WordHint } from "."
import { ChatMessage } from "./chat"

export interface RoundStateChangedEvent {
  type: "ROUND_STATE_CHANGED"
  state: RoundState
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
  | RoundStateChangedEvent
  | RoundAssignedDrawerEvent
  | RoundAssignedGuesserEvent
  | RoundChatMessageEvent
  | RoundCorrectGuessEvent
  | RoundCorrectGuessesUpdatedEvent
