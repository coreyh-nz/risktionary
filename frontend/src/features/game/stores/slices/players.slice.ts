import { GameSessionPlayer } from "@/features/game/types/game"
import { StateCreator } from "zustand"

export interface PlayersSlice {
  players: GameSessionPlayer[]

  setPlayers: (players: GameSessionPlayer[]) => void
  addPlayer: (player: GameSessionPlayer) => void
  removePlayer: (playerId: string) => void

  resetPlayers: () => void
}

export const createPlayersSlice: StateCreator<
  PlayersSlice,
  [],
  [],
  PlayersSlice
> = (set) => ({
  players: [],

  setPlayers: (players) => set({ players }),
  addPlayer: (player) =>
    set((state) => ({
      players: state.players.some((p) => p.id === player.id)
        ? state.players
        : [...state.players, player],
    })),
  removePlayer: (playerId) =>
    set((state) => ({
      players: state.players.filter((p) => p.id !== playerId),
    })),

  resetPlayers: () => set({ players: [] }),
})
