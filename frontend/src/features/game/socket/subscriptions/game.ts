import { useGameStore } from "@/features/game/stores/game-store"
import { stompHandler } from "@/lib/stomp-utils"
import { assertNever } from "@/lib/utils"
import { Client } from "@stomp/stompjs"
import {
  GameEvent,
  GameStateEvent,
  PlayerJoinedEvent,
  PlayerLeftEvent,
  PlayerListUpdatedEvent,
  VolunteersUpdatedEvent,
} from "../../types/game/events"
import {
  mapGameStateViewToGameState,
  mapRoundViewToRound,
} from "../view-mapper"

const handleGameEvent = (event: GameEvent) => {
  const type = event.type
  switch (type) {
    case "STATE": {
      handleState(event)
      break
    }
    case "PLAYER_JOINED": {
      handlePlayerJoinedEvent(event)
      break
    }
    case "PLAYER_LEFT": {
      handlePlayerLeftEvent(event)
      break
    }
    case "PLAYER_LIST_UPDATED": {
      handlePlayerList(event)
      break
    }
    case "VOLUNTEERS_UPDATED": {
      handleVolunteersUpdated(event)
      break
    }
    default:
      assertNever(type)
  }
}

const handleState = ({ state }: GameStateEvent) => {
  const gameState = mapGameStateViewToGameState(state)
  useGameStore.getState().setState(gameState)

  // round state is stored separately to game state
  if (state.type === "IN_PROGRESS") {
    const round = mapRoundViewToRound(state.round)
    const previous = useGameStore.getState().round
    // a new round: drop the previous round's chat, ratings, canvas, etc.
    // (same number means a resync of the current round, so keep it)
    if (previous && previous.number !== round.number) {
      useGameStore.getState().resetForNewRound()
    }
    useGameStore.getState().setRound(round)
  }
}

const handlePlayerList = ({ players }: PlayerListUpdatedEvent) => {
  useGameStore.getState().setPlayers(players)
}

const handlePlayerJoinedEvent = ({ player }: PlayerJoinedEvent) => {
  useGameStore.getState().addPlayer(player)
}

const handlePlayerLeftEvent = ({ playerId }: PlayerLeftEvent) => {
  useGameStore.getState().removePlayer(playerId)
}

const handleVolunteersUpdated = (event: VolunteersUpdatedEvent) => {
  useGameStore.getState().setVolunteers(event.volunteers)
}

export const setupGameSubscriptions = (client: Client, gameId: string) => {
  client.subscribe(
    `/topic/game/${gameId}`,
    stompHandler<GameEvent>(handleGameEvent)
  )
  client.subscribe(`/user/queue/game`, stompHandler<GameEvent>(handleGameEvent))
}
