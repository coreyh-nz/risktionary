import { GameState } from "./game"
import { RoundState } from "./round"

export type GameEventType =
  | "STATE_CHANGED"
  | "ROUND_STATE_CHANGED"
  | "VOLUNTEERS_UPDATED"

export interface GameEvent {
  type: GameEventType
}

export interface StateChangedEvent extends GameEvent {
  state: GameState
}

export interface RoundStateChangedEvent extends GameEvent {
  state: RoundState
}

export interface VolunteersUpdatedEvent extends GameEvent {
  volunteers: string[]
}
