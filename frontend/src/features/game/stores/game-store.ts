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
import { createRoundSlice, RoundSlice } from "./slices/round.slice"
import {
  createVolunteersSlice,
  VolunteersSlice,
} from "./slices/volunteers.slice"

type GameStore = SessionSlice &
  GameStateSlice &
  PlayersSlice &
  VolunteersSlice &
  RoundSlice & {
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

    reset: () => {
      get().resetState()
      get().resetSession()
      get().resetPlayers()
      get().resetVolunteers()
      get().resetRound()
    },
  }
}

export const useGameStore = config.persistGameSession
  ? create<GameStore>()(
      persist(storeDefinition, {
        name: "game-session-dev",
        storage: createJSONStorage(() => localStorage),
      })
    )
  : create<GameStore>()(storeDefinition)
