import { Client } from "@stomp/stompjs"
import {
  PlayerEvent,
  PlayerJoinedEvent,
  PlayerListUpdatedEvent,
} from "@/types/game/game-player-events"
import { stompHandler } from "@/lib/stomp-utils"
import { useGameStore } from "@/stores/game-store"

const handlePlayerEvent = (event: PlayerEvent) => {
  switch (event.type) {
    case "PLAYER_JOINED": {
      handlePlayerJoinedEvent(event as PlayerJoinedEvent)
      break
    }
  }
}

const handlePlayerList = ({ players }: PlayerListUpdatedEvent) => {
  useGameStore.getState().setPlayers(players)
}

const handlePlayerJoinedEvent = ({ player }: PlayerJoinedEvent) =>
  useGameStore.getState().addPlayer(player)

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
