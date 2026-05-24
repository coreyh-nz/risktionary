export type PlayerMessage = {
  type: "PLAYER"
  playerId: string
  playerDisplayName: string
  text: string
}

export type PlayerGuessCorrectlySystemMessage = {
  type: "SYSTEM"
  kind: "PLAYER_GUESSED_CORRECTLY"
  playerId: string
  playerDisplayName: string
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
  | PlayerGuessCorrectlySystemMessage
  | DrawingEndedAllGuessedSystemMessage
  | DrawingEndedTimeUpSystemMessage

export type ChatMessage = PlayerMessage | SystemMessage
