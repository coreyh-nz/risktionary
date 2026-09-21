import {
  Feedback,
  FeedbackGuess,
} from "@/features/game/stores/slices/feedback.slice"

export interface FeedbackEntry {
  id: string
  kind: "GUESS" | "SUMMARY"
  title: string
  text: string
  time: number
}

export interface FeedbackRoundGroup {
  round: number
  entries: FeedbackEntry[]
}

// Groups all received feedback by round, newest round first.
export const buildFeedbackGroups = (
  byMessage: Record<string, Feedback>,
  byRound: Record<number, Feedback>,
  guesses: Record<string, FeedbackGuess>
): FeedbackRoundGroup[] => {
  const map = new Map<number, FeedbackEntry[]>()
  const push = (round: number, entry: FeedbackEntry) =>
    map.set(round, [...(map.get(round) ?? []), entry])

  Object.entries(byMessage).forEach(([messageId, f]) => {
    const guess = guesses[messageId]
    push(f.roundNumber, {
      id: f.id,
      kind: "GUESS",
      title: guess?.text ? `"${guess.text}"` : "Correct guess",
      text: f.text,
      time: f.receivedAt,
    })
  })
  Object.values(byRound).forEach((f) =>
    push(f.roundNumber, {
      id: f.id,
      kind: "SUMMARY",
      title: "Round summary",
      text: f.text,
      time: f.receivedAt,
    })
  )

  return [...map.entries()]
    .sort(([a], [b]) => b - a)
    .map(([round, entries]) => ({
      round,
      entries: entries.sort((a, b) => a.time - b.time),
    }))
}
