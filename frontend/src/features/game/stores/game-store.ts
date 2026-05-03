import {
  createSessionSlice,
  SessionSlice,
} from "@/features/game/stores/slices/session.slice"
import {
  createPlayersSlice,
  PlayersSlice,
} from "@/features/game/stores/slices/players.slice"
import { create, StateCreator } from "zustand"
import { config } from "@/lib/config"
import { createJSONStorage, persist } from "zustand/middleware"
import {
  createGamePhaseSlice,
  GamePhaseSlice,
} from "@/features/game/stores/slices/phase.slice"

type GameStore = SessionSlice &
  GamePhaseSlice &
  PlayersSlice & {
    reset: () => void
  }

const storeDefinition: StateCreator<GameStore> = (...args) => {
  const [, get] = args
  return {
    ...createSessionSlice(...args),
    ...createGamePhaseSlice(...args),
    ...createPlayersSlice(...args),

    reset: () => {
      get().resetPhase()
      // get().resetSession()
      get().resetPlayers()
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
