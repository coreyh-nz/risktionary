import { GameSessionPlayer } from "@/features/game/types/game"

export type PlayerMessage = {
  type: "PLAYER"
  player: GameSessionPlayer
  text: string
}

export type DrawerSelectedSystemMessage = {
  type: "SYSTEM"
  kind: "DRAWER_SELECTED"
  player: GameSessionPlayer
}

export type PlayerGuessCorrectlySystemMessage = {
  type: "SYSTEM"
  kind: "PLAYER_GUESSED_CORRECTLY"
  player: GameSessionPlayer
}

export type DrawingEndedAllGuessedSystemMessage = {
  type: "SYSTEM"
  kind: "DRAWING_ENDED_ALL_GUESSED"
  word: string
}

export type DrawingEndedTimeUpSystemMessage = {
  type: "SYSTEM"
  kind: "DRAWING_ENDED_TIME_UP"
  word: string
}

export type SystemMessage =
  | DrawerSelectedSystemMessage
  | PlayerGuessCorrectlySystemMessage
  | DrawingEndedAllGuessedSystemMessage
  | DrawingEndedTimeUpSystemMessage

export type ChatMessage = PlayerMessage | SystemMessage
