import { UserSession } from "@/features/game/types/game"
import { StateCreator } from "zustand"

export interface SessionSlice {
  session: UserSession | null

  setHost: (gameId: string, gameCode: string) => void
  setPlayer: (
    gameId: string,
    gameCode: string,
    ticket: string,
    playerId: string,
    displayName: string
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

  setHost: (gameId, gameCode) =>
    set({ session: { role: "host", gameId, gameCode } }),
  setPlayer: (gameId, gameCode, ticket, playerId, displayName) =>
    set({
      session: {
        role: "player",
        gameId,
        gameCode,
        ticket,
        playerId,
        displayName,
      },
    }),
  clearSession: () => set({ session: null }),

  resetSession: () => set({ session: null }),
})
