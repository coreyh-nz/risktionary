import { StateCreator } from "zustand"
import { RoundState } from "../../types/round"

export interface RoundSlice {
  roundState: RoundState | null
  setRoundState: (state: RoundState) => void
  resetRound: () => void

  volunteers: string[]
  addVolunteer: (playerId: string) => void
  removeVolunteer: (playerId: string) => void
}

export const createRoundSlice: StateCreator<RoundSlice, [], [], RoundSlice> = (
  set
) => ({
  roundState: null,
  setRoundState: (roundState) => set({ roundState }),
  resetRound: () => set({ roundState: null }),

  volunteers: [],
  addVolunteer: (playerId: string) =>
    set((state) => ({
      volunteers: [...state.volunteers, playerId],
    })),
  removeVolunteer: (playerId: string) =>
    set((state) => ({
      volunteers: state.volunteers.filter((id) => id !== playerId),
    })),
})
