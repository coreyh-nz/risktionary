import { Client } from "@stomp/stompjs"
import { stompHandler } from "@/lib/stomp-utils"
import { useGameStore } from "@/features/game/stores/game-store"
import { GameEvent, StateChangedEvent } from "../../types/game-events"
import { instantToDate } from "@/types/instant"
import { GameState, GameStateWire } from "../../types/game"

const handleGameEvent = (event: GameEvent) => {
  switch (event.type) {
    case "STATE_CHANGED": {
      handleStateChanged(event as StateChangedEvent)
      break
    }
  }
}

const handleStateChanged = (event: StateChangedEvent) => {
  useGameStore.getState().setState(toGameState(event.state))
}

const toGameState = (wire: GameStateWire): GameState => {
  if (wire.type === "STARTING") {
    const { startingAt, ...rest } = wire
    return { ...rest, startingAt: instantToDate(startingAt) }
  }
  return wire
}

export const setupGameSubscriptions = (client: Client, gameId: string) => {
  client.subscribe(
    `/topic/game/${gameId}`,
    stompHandler<GameEvent>(handleGameEvent)
  )
}
