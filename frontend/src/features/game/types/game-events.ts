import { GameState } from "./game"

export type GameEventType = "STATE_CHANGED" | "VOLUNTEERS_UPDATED"

export interface GameEvent {
  type: GameEventType
}

export interface StateChangedEvent extends GameEvent {
  state: GameState
}

export interface VolunteersUpdatedEvent extends GameEvent {
  volunteers: string[]
}
