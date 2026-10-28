import { StateCreator } from "zustand"

export interface Feedback {
  id: string
  messageId: string | null
  roundNumber: number
  text: string
  receivedAt: number
}

// One of the player's own guesses. `text` is null for a correct guess.
export interface FeedbackGuess {
  id: string
  roundNumber: number
  text: string | null
}

export interface FeedbackSlice {
  // keyed by message id, so feedback arriving before its message is buffered
  feedbackByMessageId: Record<string, Feedback>
  // round-level feedback, keyed by round number
  feedbackByRound: Record<number, Feedback>
  feedbackGuesses: Record<string, FeedbackGuess>
  // feedback received after this time is unread
  feedbackSeenAt: number

  addFeedback: (feedback: Feedback) => void
  addFeedbackGuess: (guess: FeedbackGuess) => void
  markFeedbackSeen: () => void
  resetFeedback: () => void
}

export const createFeedbackSlice: StateCreator<
  FeedbackSlice,
  [],
  [],
  FeedbackSlice
> = (set) => ({
  feedbackByMessageId: {},
  feedbackByRound: {},
  feedbackGuesses: {},
  feedbackSeenAt: 0,

  addFeedback: (feedback) =>
    set((state) =>
      feedback.messageId
        ? {
            feedbackByMessageId: {
              ...state.feedbackByMessageId,
              [feedback.messageId]: feedback,
            },
          }
        : {
            feedbackByRound: {
              ...state.feedbackByRound,
              [feedback.roundNumber]: feedback,
            },
          }
    ),
  addFeedbackGuess: (guess) =>
    set((state) => ({
      feedbackGuesses: { ...state.feedbackGuesses, [guess.id]: guess },
    })),

  markFeedbackSeen: () => set({ feedbackSeenAt: Date.now() }),

  resetFeedback: () =>
    set({
      feedbackByMessageId: {},
      feedbackByRound: {},
      feedbackGuesses: {},
      feedbackSeenAt: 0,
    }),
})
