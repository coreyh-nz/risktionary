import { GameSessionPlayer } from "@/types/game"

export interface PlayerEvent {
  type: string
}

export interface PlayerJoinedEvent extends PlayerEvent {
  player: GameSessionPlayer
}

export interface PlayerListUpdatedEvent extends PlayerEvent {
  players: GameSessionPlayer[]
}
