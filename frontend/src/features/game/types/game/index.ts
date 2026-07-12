export type GameState =
  | { type: "INITIALIZING" }
  | { type: "LOBBY" }
  | { type: "STARTING"; startingInMs: number }
  | { type: "IN_PROGRESS" }
  | { type: "COMPLETED" }

export interface GameSessionHostView {
  id: string
  code: string
  state: GameState
}

export interface GameSessionPlayerView {
  id: string
  code: string
  state: GameState
}

export interface HostSession {
  role: "host"
  gameId: string
  gameCode: string
}

export interface PlayerSession {
  role: "player"
  gameId: string
  gameCode: string
  playerId: string
  displayName: string
  ticket: string
}

export type UserSession = HostSession | PlayerSession

export type GameSession = GameSessionHostView | GameSessionPlayerView

export interface GameSessionPlayer {
  id: string
  displayName: string
}
