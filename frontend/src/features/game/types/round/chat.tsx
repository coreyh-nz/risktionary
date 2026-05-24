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

export type SystemMessage = PlayerGuessCorrectlySystemMessage

export type ChatMessage = PlayerMessage | SystemMessage
