import { config } from "@/lib/config"
import { GameState, UserSession } from "@/types/game"
import { create, StateCreator } from "zustand"
import { createJSONStorage, persist } from "zustand/middleware"

interface GameStore {
  session: UserSession | null
  state: GameState

  setHost: (gameId: string, gameCode: string) => void
  setPlayer: (
    gameCode: string,
    playerId: string,
    displayName: string,
    ticket: string
  ) => void
  clear: () => void
}

const storeDefinition: StateCreator<GameStore> = (set) => ({
  session: null,
  isHost: false,
  state: "LOBBY",

  setHost: (gameId, gameCode) =>
    set({ session: { role: "host", gameId, gameCode } }),

  setPlayer: (gameCode, playerId, displayName, ticket) =>
    set({
      session: { gameCode, role: "player", playerId, displayName, ticket },
    }),

  clear: () => set({ session: null }),
})

export const useGameStore = config.persistGameSession
  ? create<GameStore>()(
      persist(storeDefinition, {
        name: "game-session-dev",
        storage: createJSONStorage(() => localStorage),
      })
    )
  : create<GameStore>()(storeDefinition)
