import { stompHandler } from "@/lib/stomp-utils"
import { assertNever } from "@/lib/utils"
import { Client } from "@stomp/stompjs"
import { useGameStore } from "../../stores/game-store"
import {
  RoundAssignedDrawerEvent,
  RoundAssignedGuesserEvent,
  RoundChatMessageEvent,
  RoundCorrectGuessesUpdatedEvent,
  RoundCorrectGuessEvent,
  RoundEvent,
  RoundStateEvent,
  RoundStateView,
} from "../../types/round/phase/events"
import { RoundState } from "../../types/round/phase/round"
import { mapRoundStateViewToRoundState } from "../view-mapper"

const handleRoundEvent = (event: RoundEvent) => {
  const type = event.type
  switch (type) {
    case "STATE":
      handleRoundStateEvent(event)
      break
    case "ASSIGNED_DRAWER":
      handleAssignedDrawerEvent(event)
      break
    case "ASSIGNED_GUESSER":
      handleAssignedGuesserEvent(event)
      break
    case "CHAT_MESSAGE":
      handleChatMessageEvent(event)
      break
    case "CORRECT_GUESS":
      handleCorrectGuessEvent(event)
      break
    case "CORRECT_GUESSES_COUNT":
      handleCorrectGuessesUpdatedEvent(event)
      break
    default:
      assertNever(type)
  }
}

const handleRoundStateEvent = (event: RoundStateEvent) => {
  handleRoundState(event.state)
}

const handleAssignedGuesserEvent = (event: RoundAssignedGuesserEvent) => {
  useGameStore.getState().setRoundGuesser(event.hint)
}

const handleAssignedDrawerEvent = (event: RoundAssignedDrawerEvent) => {
  useGameStore.getState().setRoundDrawer(event.word)
}

const handleChatMessageEvent = (event: RoundChatMessageEvent) => {
  useGameStore.getState().addRoundChatMessage(event.message)
}

const handleCorrectGuessEvent = (event: RoundCorrectGuessEvent) => {
  const role = useGameStore.getState().roundRole
  if (role?.type === "GUESSER") {
    useGameStore.getState().setRoundGuesserCorrectWord(event.word)
  }
}

const handleCorrectGuessesUpdatedEvent = (
  event: RoundCorrectGuessesUpdatedEvent
) => {
  useGameStore.getState().setRoundCorrectGuessesCount(event.correctGuesses)
}

export const handleRoundState = (stateView: RoundStateView) => {
  const state: RoundState = mapRoundStateViewToRoundState(stateView)
  useGameStore.getState().setRoundState(state)
}

export const setupRoundSubscriptions = (client: Client, gameId: string) => {
  client.subscribe(
    "/user/queue/game/round",
    stompHandler<RoundEvent>(handleRoundEvent)
  )
  client.subscribe(
    `/topic/game/${gameId}/round`,
    stompHandler<RoundEvent>(handleRoundEvent)
  )
}
