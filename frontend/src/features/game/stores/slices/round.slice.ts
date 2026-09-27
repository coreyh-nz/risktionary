import { ChatMessage } from "@/features/game/types/round/phase/drawing/chat"
import {
  RiskRating,
  RiskRatingCount,
} from "@/features/game/types/round/phase/risk/risk"
import { StateCreator } from "zustand"
import { Round, RoundRole, WordHint } from "../../types/round/phase/round"

export interface RoundSlice {
  round: Round | null
  roundRole: RoundRole | null
  roundChatMessages: ChatMessage[]
  roundCorrectGuessesCount: number
  roundRiskRatingCounts: RiskRatingCount[] | null
  roundSubmittedRiskRating: RiskRating | null

  setRound: (round: Round) => void
  setRoundDrawer: (word: string) => void
  setRoundGuesser: (hint: WordHint) => void
  setRoundGuesserCorrectWord: (word: string) => void
  setRoundRiskRatingCounts: (riskRatingCounts: RiskRatingCount[]) => void
  setRoundCorrectGuessesCount: (roundCorrectGuessesCount: number) => void
  setRoundSubmittedRiskRating: (riskRating: RiskRating) => void

  addRoundChatMessage: (message: ChatMessage) => void

  // clears everything belonging to a single round, keeping the round itself
  resetRoundData: () => void
  resetRound: () => void
}

export const createRoundSlice: StateCreator<RoundSlice, [], [], RoundSlice> = (
  set
) => ({
  round: null,
  roundRole: null,
  roundChatMessages: [],
  roundCorrectGuessesCount: 0,
  roundRiskRatingCounts: null,
  roundSubmittedRiskRating: null,

  setRound: (round) => set({ round }),
  setRoundDrawer: (word) => set({ roundRole: { type: "DRAWER", word } }),
  setRoundGuesser: (hint) => set({ roundRole: { type: "GUESSER", hint } }),
  setRoundGuesserCorrectWord: (word: string) =>
    set((state) => {
      if (state.roundRole?.type !== "GUESSER") return state
      return { roundRole: { ...state.roundRole, correctGuessWord: word } }
    }),

  setRoundCorrectGuessesCount: (roundCorrectGuessesCount: number) =>
    set({ roundCorrectGuessesCount }),

  setRoundRiskRatingCounts: (riskRatingCounts: RiskRatingCount[]) =>
    set({ roundRiskRatingCounts: riskRatingCounts }),

  setRoundSubmittedRiskRating: (riskRating: RiskRating) =>
    set({ roundSubmittedRiskRating: riskRating }),

  addRoundChatMessage: (message) =>
    set((state) => ({
      roundChatMessages: [...state.roundChatMessages, message],
    })),

  resetRoundData: () =>
    set({
      roundRole: null,
      roundChatMessages: [],
      roundCorrectGuessesCount: 0,
      roundRiskRatingCounts: null,
      roundSubmittedRiskRating: null,
    }),

  resetRound: () =>
    set({
      round: null,
      roundRole: null,
      roundChatMessages: [],
      roundCorrectGuessesCount: 0,
      roundRiskRatingCounts: null,
      roundSubmittedRiskRating: null,
    }),
})
