import { StateCreator } from "zustand"
import { RoundPhase, RoundRole, RoundState, WordHint } from "../../types/round"
import { ChatMessage } from "../../types/round/chat"

export interface RoundSlice {
  roundState: RoundState | null
  roundPhase: RoundPhase | null
  roundRole: RoundRole | null
  roundChatMessages: ChatMessage[]
  roundCorrectGuessesCount: number

  setRoundState: (state: RoundState) => void
  setRoundPhase: (phase: RoundPhase) => void

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
  roundState: null,
  roundPhase: null,
  roundRole: null,
  roundChatMessages: [],
  roundCorrectGuessesCount: 0,

  setRoundState: (roundState) => set({ roundState }),
  setRoundPhase: (roundPhase) => set({ roundPhase }),

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
      roundState: null,
      roundRole: null,
      roundChatMessages: [],
      roundCorrectGuessesCount: 0,
    }),
})
