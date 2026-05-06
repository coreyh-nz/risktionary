import { GameStateWire } from "./game"

export type GameEventType = "STATE_CHANGED"

export interface GameEvent {
  type: GameEventType
}

export interface StateChangedEvent extends GameEvent {
  state: GameStateWire
}
