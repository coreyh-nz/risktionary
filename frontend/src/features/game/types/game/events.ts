import { GameSessionPlayer } from "."
import { RoundView } from "../round/phase/events"

export type GameStateView =
  | {
      type: "LOBBY"
    }
  | {
      type: "STARTING"
      startingInMs: number
    }
  | {
      type: "IN_PROGRESS"
      round: RoundView
    }
  | {
      type: "COMPLETED"
    }

export interface GameStateEvent {
  type: "STATE"
  state: GameStateView
}

export interface PlayerJoinedEvent {
  type: "PLAYER_JOINED"
  player: GameSessionPlayer
}

export interface PlayerLeftEvent {
  type: "PLAYER_LEFT"
  playerId: string
}

export interface PlayerListUpdatedEvent {
  type: "PLAYER_LIST_UPDATED"
  players: GameSessionPlayer[]
}

export interface VolunteersUpdatedEvent {
  type: "VOLUNTEERS_UPDATED"
  volunteers: string[]
}

export type GameEvent =
  | GameStateEvent
  | PlayerJoinedEvent
  | PlayerLeftEvent
  | PlayerListUpdatedEvent
  | VolunteersUpdatedEvent
