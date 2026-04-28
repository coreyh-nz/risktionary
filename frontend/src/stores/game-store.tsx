import { config } from "@/lib/config"
import { GameState, UserSession } from "@/types/game"
import { create, StateCreator } from "zustand"
import { createJSONStorage, persist } from "zustand/middleware"

interface GameStore {
  session: UserSession | null
  state: GameState

  setHost: (gameId: string, gameCode: string) => void
  setPlayer: (
    gameId: string,
    gameCode: string,
    ticket: string,
    playerId: string,
    displayName: string
  ) => void
  clear: () => void
}

const storeDefinition: StateCreator<GameStore> = (set) => ({
  session: null,
  isHost: false,
  state: "LOBBY",

  setHost: (gameId, gameCode) =>
    set({ session: { role: "host", gameId, gameCode } }),

  setPlayer: (gameId, gameCode, ticket, playerId, displayName) =>
    set({
      session: {
        gameId,
        gameCode,
        role: "player",
        playerId,
        displayName,
        ticket,
      },
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
