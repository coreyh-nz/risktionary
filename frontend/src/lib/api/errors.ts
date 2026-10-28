export const ErrorCode = {
  // Auth
  AUTH_UNAUTHENTICATED: "auth.unauthenticated",
  AUTH_FORBIDDEN: "auth.forbidden",

  // Game
  GAME_NOT_FOUND: "game.not-found",
  GAME_CODE_GENERATION_FAILED: "game.code-generation-failed",
  GAME_CODE_IN_USE: "game.code-in-use",
  GAME_PLAYER_ALREADY_IN_GAME: "game.player-already-in-game",
  GAME_PLAYER_NOT_IN_GAME: "game.player-not-in-game",
  GAME_PLAYER_DISPLAY_NAME_IN_USE: "game.player-display-name-in-use",

  // Generic
  INTERNAL_ERROR: "generic.internal-error",
  INVALID_REQUEST: "generic.bad-request",
  NOT_FOUND: "generic.not-found",
} as const

export type ErrorCode = (typeof ErrorCode)[keyof typeof ErrorCode]

export class ApiError extends Error {
  constructor(
    public readonly errorCode: ErrorCode | string,
    message: string,
    public readonly status: number
  ) {
    super(message)
    this.name = "ApiError"
  }

  is(code: ErrorCode) {
    return this.errorCode === code
  }
}
