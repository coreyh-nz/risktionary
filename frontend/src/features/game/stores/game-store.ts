import {
  createPlayersSlice,
  PlayersSlice,
} from "@/features/game/stores/slices/players.slice"
import {
  createSessionSlice,
  SessionSlice,
} from "@/features/game/stores/slices/session.slice"
import {
  createGamePhaseSlice,
  GameStateSlice,
} from "@/features/game/stores/slices/state.slice"
import { create, StateCreator } from "zustand"
import { createJSONStorage, persist } from "zustand/middleware"
import { drawingStore } from "./drawing-store"
import { createFeedbackSlice, FeedbackSlice } from "./slices/feedback.slice"
import { createRoundSlice, RoundSlice } from "./slices/round.slice"
import {
  createVolunteersSlice,
  VolunteersSlice,
} from "./slices/volunteers.slice"

type GameStore = SessionSlice &
  GameStateSlice &
  PlayersSlice &
  VolunteersSlice &
  RoundSlice &
  FeedbackSlice & {
    // clears per-round state (chat, ratings, canvas, etc.) when a new round starts.
    // feedback is kept, the history panel spans rounds.
    resetForNewRound: () => void
    reset: () => void
  }

const storeDefinition: StateCreator<GameStore> = (...args) => {
  const [, get] = args
  return {
    ...createSessionSlice(...args),
    ...createGamePhaseSlice(...args),
    ...createPlayersSlice(...args),
    ...createVolunteersSlice(...args),
    ...createRoundSlice(...args),
    ...createFeedbackSlice(...args),

    resetForNewRound: () => {
      get().resetRoundData()
      get().resetVolunteers()
      drawingStore.getState().clearCanvas()
    },

    reset: () => {
      get().resetState()
      get().resetSession()
      get().resetPlayers()
      get().resetVolunteers()
      get().resetRound()
      get().resetFeedback()
      drawingStore.getState().clearCanvas()
    },
  }
}

// Only the session is persisted (per tab, so a refresh reconnects but two tabs
// can still be two different players). Everything else is rebuilt from the
// events the server sends after we reconnect.
export const useGameStore = create<GameStore>()(
  persist(storeDefinition, {
    name: "risktionary-game-session",
    storage: createJSONStorage(() => sessionStorage),
    partialize: (state) => ({
      session: state.session,
      feedbackEnabled: state.feedbackEnabled,
    }),
  })
)
