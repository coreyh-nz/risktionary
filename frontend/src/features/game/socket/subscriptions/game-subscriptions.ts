import { Client } from "@stomp/stompjs"
import { stompHandler } from "@/lib/stomp-utils"
import { useGameStore } from "@/features/game/stores/game-store"
import { GameEvent, StateChangedEvent } from "../../types/game-events"

const handleGameEvent = (event: GameEvent) => {
  switch (event.type) {
    case "STATE_CHANGED": {
      handleStateChanged(event as StateChangedEvent)
      break
    }
  }
}

const handleStateChanged = (event: StateChangedEvent) => {
  useGameStore.getState().setState(event.state)
}

export const setupGameSubscriptions = (client: Client, gameId: string) => {
  client.subscribe(
    `/topic/game/${gameId}`,
    stompHandler<GameEvent>(handleGameEvent)
  )
}
