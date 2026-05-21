import { stompHandler } from "@/lib/stomp-utils"
import { assertNever } from "@/lib/utils"
import { Client } from "@stomp/stompjs"
import { useGameStore } from "../../stores/game-store"
import {
  RoundAssignedDrawerEvent,
  RoundAssignedGuesserEvent,
  RoundEvent,
} from "../../types/round/events"

const handleRoundEvent = (event: RoundEvent) => {
  const type = event.type
  switch (type) {
    case "ASSIGNED_DRAWER":
      handleAssignedDrawerEvent(event)
      break
    case "ASSIGNED_GUESSER":
      handleAssignedGuesserEvent(event)
      break
    default:
      assertNever(type)
  }
}

const handleAssignedGuesserEvent = (event: RoundAssignedGuesserEvent) => {
  useGameStore.getState().setRoundGuesser(event.hint)
}

const handleAssignedDrawerEvent = (event: RoundAssignedDrawerEvent) => {
  useGameStore.getState().setRoundDrawer(event.word)
}

export const setupRoundSubscriptions = (client: Client) => {
  client.subscribe(
    "/user/queue/round",
    stompHandler<RoundEvent>(handleRoundEvent)
  )
}
