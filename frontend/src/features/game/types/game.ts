export type GameStateType =
  | "INITIALIZING"
  | "LOBBY"
  | "STARTING"
  | "IN_PROGRESS"
  | "PAUSED"
  | "COMPLETED"

type GameStateDataMap = {
  STARTING: { startingIn: number }
}

export type GameState = {
  [K in GameStateType]: K extends keyof GameStateDataMap
    ? { type: K } & GameStateDataMap[K]
    : { type: K }
}[GameStateType]

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
