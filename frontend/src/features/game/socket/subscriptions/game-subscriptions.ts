import { useGameStore } from "@/features/game/stores/game-store"
import { stompHandler } from "@/lib/stomp-utils"
import { Client } from "@stomp/stompjs"
import {
  GameEvent,
  RoundStateChangedEvent,
  StateChangedEvent,
  VolunteersUpdatedEvent,
} from "../../types/game-events"

const handleGameEvent = (event: GameEvent) => {
  switch (event.type) {
    case "STATE_CHANGED": {
      handleStateChanged(event as StateChangedEvent)
      break
    }
    case "ROUND_STATE_CHANGED": {
      handleRoundStateChanged(event as RoundStateChangedEvent)
      break
    }
    case "VOLUNTEERS_UPDATED": {
      handleVolunteersUpdated(event as VolunteersUpdatedEvent)
      break
    }
  }
}

const handleStateChanged = (event: StateChangedEvent) => {
  useGameStore.getState().setState(event.state)
}

const handleRoundStateChanged = (event: RoundStateChangedEvent) => {
  useGameStore.getState().setRoundState(event.state)
}

const handleVolunteersUpdated = (event: VolunteersUpdatedEvent) => {
  useGameStore.getState().setVolunteers(event.volunteers)
}

export const setupGameSubscriptions = (client: Client, gameId: string) => {
  client.subscribe(
    `/topic/game/${gameId}`,
    stompHandler<GameEvent>(handleGameEvent)
  )
}
