import { ChatMessage } from "@/features/game/types/round/phase/drawing/chat"
import { StateCreator } from "zustand"
import { Round, RoundRole, WordHint } from "../../types/round/phase/round"

export interface RoundSlice {
  round: Round | null
  roundRole: RoundRole | null
  roundChatMessages: ChatMessage[]
  roundCorrectGuessesCount: number

  setRound: (round: Round) => void
  setRoundDrawer: (word: string) => void
  setRoundGuesser: (hint: WordHint) => void
  setRoundGuesserCorrectWord: (word: string) => void

  addRoundChatMessage: (message: ChatMessage) => void

  setRoundCorrectGuessesCount: (roundCorrectGuessesCount: number) => void

  resetRound: () => void
}

export const createRoundSlice: StateCreator<RoundSlice, [], [], RoundSlice> = (
  set
) => ({
  round: null,
  roundRole: null,
  roundChatMessages: [],
  roundCorrectGuessesCount: 0,

  setRound: (round) => set({ round }),

  setRoundDrawer: (word) => set({ roundRole: { type: "DRAWER", word } }),
  setRoundGuesser: (hint) => set({ roundRole: { type: "GUESSER", hint } }),
  setRoundGuesserCorrectWord: (word: string) =>
    set((state) => {
      if (state.roundRole?.type !== "GUESSER") return state
      return { roundRole: { ...state.roundRole, correctGuessWord: word } }
    }),

  addRoundChatMessage: (message) =>
    set((state) => ({
      roundChatMessages: [...state.roundChatMessages, message],
    })),

  setRoundCorrectGuessesCount: (roundCorrectGuessesCount: number) =>
    set({ roundCorrectGuessesCount }),

  resetRound: () =>
    set({
      round: null,
      roundRole: null,
      roundChatMessages: [],
      roundCorrectGuessesCount: 0,
    }),
})
