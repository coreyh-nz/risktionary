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
  RoundFeedbackEvent,
  RoundPhaseStateEvent,
  RoundRiskRatingsUpdatedEvent,
  RoundStateEvent,
} from "../../types/round/phase/events"
import { RoundState } from "../../types/round/phase/round"
import {
  mapRoundPhaseStateViewToRoundPhaseState,
  mapRoundStateViewToRoundState,
} from "../view-mapper"

const handleRoundEvent = (event: RoundEvent) => {
  const type = event.type
  switch (type) {
    case "STATE":
      handleRoundStateEvent(event)
      break
    case "PHASE_STATE":
      handleRoundPhaseStateEvent(event)
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
    case "RISK_RATINGS_UPDATED":
      handleRiskRatingsUpdatedEvent(event)
      break
    case "FEEDBACK":
      handleFeedbackEvent(event)
      break
    default:
      assertNever(type)
  }
}

const handleRoundStateEvent = (event: RoundStateEvent) => {
  const round = useGameStore.getState().round
  if (!round) throw new Error()

  const state: RoundState = mapRoundStateViewToRoundState(event.state)
  useGameStore.getState().setRound({
    ...round,
    state,
  })
}

const handleRoundPhaseStateEvent = (event: RoundPhaseStateEvent) => {
  const round = useGameStore.getState().round
  if (!round) throw new Error()

  const state = round.state
  if (state.type !== "IN_PROGRESS") {
    console.error(
      "Expected round to be in progress upon receiving phase update event"
    )
    return
  }

  useGameStore.getState().setRound({
    ...round,
    state: {
      ...state,
      phase: mapRoundPhaseStateViewToRoundPhaseState(event.phase),
    },
  })
}

const handleAssignedGuesserEvent = (event: RoundAssignedGuesserEvent) => {
  useGameStore.getState().setRoundGuesser(event.hint)
}

const handleAssignedDrawerEvent = (event: RoundAssignedDrawerEvent) => {
  useGameStore.getState().setRoundDrawer(event.word)
}

const handleChatMessageEvent = (event: RoundChatMessageEvent) => {
  const state = useGameStore.getState()
  const message = event.message
  state.addRoundChatMessage(message)

  // remember our own guesses so feedback can be shown against them later
  const session = state.session
  if (!state.feedbackEnabled || session?.role !== "player") return
  const roundNumber = state.round?.number ?? 0
  if (message.type === "PLAYER" && message.player.id === session.playerId) {
    state.addFeedbackGuess({ id: message.id, roundNumber, text: message.text })
  } else if (
    message.type === "SYSTEM" &&
    message.kind === "PLAYER_GUESSED_CORRECTLY" &&
    message.player.id === session.playerId
  ) {
    state.addFeedbackGuess({ id: message.id, roundNumber, text: null })
  }
}

const handleFeedbackEvent = (event: RoundFeedbackEvent) => {
  const state = useGameStore.getState()
  if (!state.feedbackEnabled) return
  state.addFeedback({
    id: event.feedbackId,
    messageId: event.messageId,
    roundNumber: event.roundNumber,
    text: event.text,
    receivedAt: Date.now(),
  })
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

const handleRiskRatingsUpdatedEvent = (event: RoundRiskRatingsUpdatedEvent) => {
  useGameStore.getState().setRoundRiskRatingCounts(event.counts)
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
