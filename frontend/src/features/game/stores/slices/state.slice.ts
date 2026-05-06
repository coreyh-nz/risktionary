import { GameState } from "@/features/game/types/game"
import { StateCreator } from "zustand"

export interface GameStateSlice {
  state: GameState

  setState: (state: GameState) => void

  resetState: () => void
}

export const createGamePhaseSlice: StateCreator<
  GameStateSlice,
  [],
  [],
  GameStateSlice
> = (set) => ({
  state: { type: "LOBBY" },

  setState: (state) => set({ state }),

  resetState: () => set({ state: { type: "LOBBY" } }),
})
