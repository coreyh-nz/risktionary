import { WordHint } from "."

export interface RoundAssignedDrawerEvent {
  type: "ASSIGNED_DRAWER"
  word: string
}

export interface RoundAssignedGuesserEvent {
  type: "ASSIGNED_GUESSER"
  hint: WordHint
}

export type RoundEvent = RoundAssignedDrawerEvent | RoundAssignedGuesserEvent
