import { StateCreator } from "zustand"
import { RoundRole, RoundState, WordHint } from "../../types/round"

export interface RoundSlice {
  roundState: RoundState | null
  roundRole: RoundRole | null

  setRoundState: (state: RoundState) => void

  setRoundDrawer: (word: string) => void
  setRoundGuesser: (hint: WordHint) => void

  resetRound: () => void
}

export const createRoundSlice: StateCreator<RoundSlice, [], [], RoundSlice> = (
  set
) => ({
  roundState: null,
  roundRole: null,

  setRoundState: (roundState) => set({ roundState }),

  setRoundDrawer: (word) => set({ roundRole: { type: "DRAWER", word } }),
  setRoundGuesser: (hint) => set({ roundRole: { type: "GUESSER", hint } }),

  resetRound: () => set({ roundState: null }),
})
