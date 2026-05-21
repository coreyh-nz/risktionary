import { StateCreator } from "zustand"

export interface VolunteersSlice {
  volunteers: string[]
  setVolunteers: (playerIds: string[]) => void

  resetVolunteers: () => void
}

export const createVolunteersSlice: StateCreator<
  VolunteersSlice,
  [],
  [],
  VolunteersSlice
> = (set) => ({
  volunteers: [],
  setVolunteers: (playerIds: string[]) =>
    set({
      volunteers: playerIds,
    }),

  resetVolunteers: () => set({ volunteers: [] }),
})
