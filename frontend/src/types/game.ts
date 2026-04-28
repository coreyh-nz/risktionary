export type GameState =
  | "INITIALIZING"
  | "LOBBY"
  | "STARTING"
  | "IN_PROGRESS"
  | "PAUSED"
  | "COMPLETED"

export interface GameSessionHostView {
  id: string
  code: string
  state: GameState
}

export interface GameSessionPlayerView {
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
  gameCode: string
  playerId: string
  displayName: string
  ticket: string
}

export type UserSession = HostSession | PlayerSession

export type GameSession = GameSessionHostView | GameSessionPlayerView
