import { useGameStore } from "@/features/game/stores/game-store"
import {
  PlayerEvent,
  PlayerJoinedEvent,
  PlayerLeftEvent,
  PlayerListUpdatedEvent,
} from "@/features/game/types/game-player-events"
import { stompHandler } from "@/lib/stomp-utils"
import { Client } from "@stomp/stompjs"

const handlePlayerEvent = (event: PlayerEvent) => {
  switch (event.type) {
    case "PLAYER_JOINED": {
      handlePlayerJoinedEvent(event as PlayerJoinedEvent)
      break
    }
    case "PLAYER_LEFT": {
      handlePlayerLeftEvent(event as PlayerLeftEvent)
      break
    }
  }
}

const handlePlayerList = ({ players }: PlayerListUpdatedEvent) => {
  useGameStore.getState().setPlayers(players)
}

const handlePlayerJoinedEvent = ({ player }: PlayerJoinedEvent) =>
  useGameStore.getState().addPlayer(player)

const handlePlayerLeftEvent = ({ playerId }: PlayerLeftEvent) =>
  useGameStore.getState().removePlayer(playerId)

export const setupPlayerSubscriptions = (client: Client, gameId: string) => {
  client.subscribe(
    `/topic/game/${gameId}/players`,
    stompHandler<PlayerEvent>(handlePlayerEvent)
  )
  client.subscribe(
    `/user/queue/player-list`,
    stompHandler<PlayerListUpdatedEvent>(handlePlayerList)
  )
}
