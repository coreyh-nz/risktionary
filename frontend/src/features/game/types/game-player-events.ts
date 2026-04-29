import { GameSessionPlayer } from "@/features/game/types/game"

export type PlayerEventType =
  | "PLAYER_JOINED"
  | "PLAYER_LEFT"
  | "PLAYER_LIST_UPDATED"

export interface PlayerEvent {
  type: PlayerEventType
}

export interface PlayerJoinedEvent extends PlayerEvent {
  player: GameSessionPlayer
}

export interface PlayerLeftEvent extends PlayerEvent {
  playerId: string
}

export interface PlayerListUpdatedEvent extends PlayerEvent {
  players: GameSessionPlayer[]
}
