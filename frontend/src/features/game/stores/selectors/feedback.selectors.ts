import { useShallow } from "zustand/shallow"
import { useGameStore } from "../game-store"

export const useMessageFeedback = (messageId: string) =>
  useGameStore((s) => s.feedbackByMessageId[messageId])

export const useRoundFeedback = (roundNumber: number) =>
  useGameStore((s) => s.feedbackByRound[roundNumber])

export const useFeedbackSeenAt = () => useGameStore((s) => s.feedbackSeenAt)

export const useMarkFeedbackSeen = () => useGameStore((s) => s.markFeedbackSeen)

export const useUnreadFeedbackCount = () =>
  useGameStore((s) => {
    const unread = (f: { receivedAt: number }) => f.receivedAt > s.feedbackSeenAt
    return (
      Object.values(s.feedbackByMessageId).filter(unread).length +
      Object.values(s.feedbackByRound).filter(unread).length
    )
  })

export const useFeedbackStore = () =>
  useGameStore(
    useShallow((s) => ({
      byMessage: s.feedbackByMessageId,
      byRound: s.feedbackByRound,
      guesses: s.feedbackGuesses,
    }))
  )
