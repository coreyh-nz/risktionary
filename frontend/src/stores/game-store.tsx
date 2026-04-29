import { config } from "@/lib/config"
import { GameSessionPlayer, GameState, UserSession } from "@/types/game"
import { create, StateCreator } from "zustand"
import { createJSONStorage, persist } from "zustand/middleware"

interface GameStore {
  session: UserSession | null
  state: GameState
  players: GameSessionPlayer[]

  setHost: (gameId: string, gameCode: string) => void
  setPlayer: (
    gameId: string,
    gameCode: string,
    ticket: string,
    playerId: string,
    displayName: string
  ) => void
  clear: () => void

  setPlayers: (players: GameSessionPlayer[]) => void
  addPlayer: (player: GameSessionPlayer) => void
  removePlayer: (playerId: string) => void
}

const storeDefinition: StateCreator<GameStore> = (set) => ({
  session: null,
  isHost: false,
  state: "LOBBY",
  players: [],

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

  addPlayer: (player) =>
    set((state) => ({
      players: state.players.some((p) => p.id === player.id)
        ? state.players
        : [...state.players, player],
    })),

  setPlayers: (players) => set({ players }),

  removePlayer: (playerId) =>
    set((state) => ({
      players: state.players.filter((p) => p.id !== playerId),
    })),
})

export const useGameStore = config.persistGameSession
  ? create<GameStore>()(
      persist(storeDefinition, {
        name: "game-session-dev",
        storage: createJSONStorage(() => localStorage),
      })
    )
  : create<GameStore>()(storeDefinition)
