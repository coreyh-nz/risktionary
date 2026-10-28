import { UserSession } from "@/features/game/types/game"
import { StateCreator } from "zustand"

export interface SessionSlice {
  session: UserSession | null
  feedbackEnabled: boolean

  setHost: (gameId: string, gameCode: string) => void
  setPlayer: (
    gameId: string,
    gameCode: string,
    ticket: string,
    playerId: string,
    displayName: string,
    feedbackEnabled: boolean
  ) => void
  clearSession: () => void

  resetSession: () => void
}

export const createSessionSlice: StateCreator<
  SessionSlice,
  [],
  [],
  SessionSlice
> = (set) => ({
  session: null,
  feedbackEnabled: false,

  setHost: (gameId, gameCode) =>
    set({
      session: { role: "host", gameId, gameCode },
      feedbackEnabled: false,
    }),
  setPlayer: (
    gameId,
    gameCode,
    ticket,
    playerId,
    displayName,
    feedbackEnabled
  ) =>
    set({
      session: {
        role: "player",
        gameId,
        gameCode,
        ticket,
        playerId,
        displayName,
      },
      feedbackEnabled,
    }),
  clearSession: () => set({ session: null }),

  resetSession: () => set({ session: null, feedbackEnabled: false }),
})
