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
import { config } from "@/lib/config"
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

export const useGameStore = config.persistGameSession
  ? create<GameStore>()(
      persist(storeDefinition, {
        name: "game-session-dev",
        storage: createJSONStorage(() => sessionStorage),
        partialize: (state) => ({
          session: state.session,
          feedbackEnabled: state.feedbackEnabled,
        }),
      })
    )
  : create<GameStore>()(storeDefinition)
