import { GameState } from "@/features/game/types/game"
import { StateCreator } from "zustand"

export interface GamePhaseSlice {
  phase?: GameState

  setPhase: (phase: GameState) => void

  resetPhase: () => void
}

export const createGamePhaseSlice: StateCreator<
  GamePhaseSlice,
  [],
  [],
  GamePhaseSlice
> = (set) => ({
  phase: "LOBBY",

  setPhase: (phase) => set({ phase }),

  resetPhase: () => set({ phase: undefined }),
})
