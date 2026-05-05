import { GameState } from "@/features/game/types/game"
import { StateCreator } from "zustand"

export interface GamePhaseSlice {
  phase?: GameState
  startingAt?: Date

  setPhase: (phase: GameState) => void
  setStartingAt: (startingAt: Date) => void

  resetPhase: () => void
}

export const createGamePhaseSlice: StateCreator<
  GamePhaseSlice,
  [],
  [],
  GamePhaseSlice
> = (set) => ({
  phase: "LOBBY",
  startingAt: undefined,

  setPhase: (phase) => set({ phase }),

  setStartingAt: (startingAt: Date) => set({ startingAt }),

  resetPhase: () => set({ phase: undefined }),
})
