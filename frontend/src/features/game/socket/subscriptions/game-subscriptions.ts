import { Client } from "@stomp/stompjs"
import { stompHandler } from "@/lib/stomp-utils"
import { useGameStore } from "@/features/game/stores/game-store"
import {
  GameEvent,
  StateChangedEvent,
  StateChangedEventStarting,
} from "../../types/game-events"
import { instantToDate } from "@/types/instant"

const handleGameEvent = (event: GameEvent) => {
  switch (event.type) {
    case "STATE_CHANGED": {
      handleStateChanged(event as StateChangedEvent)
      break
    }
  }
}

const handleStateChanged = (event: StateChangedEvent) => {
  useGameStore.getState().setPhase(event.state)
  switch (event.state) {
    case "STARTING": {
      handleStateChangedEventStarting(event as StateChangedEventStarting)
      break
    }
  }
}

const handleStateChangedEventStarting = ({
  startAt: startAtInstant,
}: StateChangedEventStarting) => {
  const startAt = instantToDate(startAtInstant)
  useGameStore.getState().setStartingAt(startAt)
}

export const setupGameSubscriptions = (client: Client, gameId: string) => {
  client.subscribe(
    `/topic/game/${gameId}`,
    stompHandler<GameEvent>(handleGameEvent)
  )
}
