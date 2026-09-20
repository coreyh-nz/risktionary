"use client"

import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { AiFeedbackLabel } from "@/features/game/components/feedback/ai-feedback-label"
import {
  useGameFeedbackEnabled,
  useGameIsPlayer,
} from "@/features/game/stores/game-store-selectors"
import {
  useFeedbackStore,
  useMarkFeedbackSeen,
  useUnreadFeedbackCount,
} from "@/features/game/stores/selectors/feedback.selectors"
import { ChevronDown, History } from "lucide-react"
import { useEffect, useMemo, useState } from "react"

interface Entry {
  id: string
  title: string
  text: string
  time: number
}

const HistoryEntry = ({ entry }: { entry: Entry }) => {
  const [open, setOpen] = useState(false)
  return (
    <div className="min-w-0 rounded-md border bg-muted/40">
      <button
        type="button"
        aria-expanded={open}
        onClick={() => setOpen((o) => !o)}
        className="flex w-full min-w-0 items-start gap-2 px-3 py-2 text-left"
      >
        <div className="min-w-0 flex-1">
          <div className="text-xs font-medium wrap-break-word">
            {entry.title}
          </div>
          {!open && (
            <div className="truncate text-xs text-muted-foreground">
              {entry.text}
            </div>
          )}
        </div>
        <ChevronDown
          className={`mt-0.5 size-4 shrink-0 text-muted-foreground transition-transform ${open ? "rotate-180" : ""}`}
        />
      </button>
      {open && (
        <div className="border-t px-3 py-2">
          <AiFeedbackLabel />
          <p className="mt-1 text-sm leading-snug whitespace-pre-line">
            {entry.text}
          </p>
        </div>
      )}
    </div>
  )
}

export const FeedbackHistory = () => {
  const enabled = useGameFeedbackEnabled()
  const isPlayer = useGameIsPlayer()
  const { byMessage, byRound, guesses } = useFeedbackStore()
  const unread = useUnreadFeedbackCount()
  const markSeen = useMarkFeedbackSeen()
  const [open, setOpen] = useState(false)

  // anything that arrives while the panel is open counts as seen
  useEffect(() => {
    if (open && unread > 0) markSeen()
  }, [open, unread, markSeen])

  const groups = useMemo(() => {
    const map = new Map<number, Entry[]>()
    const push = (round: number, entry: Entry) =>
      map.set(round, [...(map.get(round) ?? []), entry])

    Object.entries(byMessage).forEach(([messageId, f]) => {
      const guess = guesses[messageId]
      push(f.roundNumber, {
        id: f.id,
        title: guess?.text ? `"${guess.text}"` : "Correct guess",
        text: f.text,
        time: f.receivedAt,
      })
    })
    Object.values(byRound).forEach((f) =>
      push(f.roundNumber, {
        id: f.id,
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
  }, [byMessage, byRound, guesses])

  if (!enabled || !isPlayer) return null

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        render={
          <Button
            variant="secondary"
            size="sm"
            className="fixed right-4 bottom-4 z-40 shadow-md"
          />
        }
      >
        <History />
        Feedback
        {unread > 0 && (
          <span className="ml-1 flex size-4 items-center justify-center rounded-full bg-primary text-[10px] font-bold text-primary-foreground tabular-nums">
            {unread}
          </span>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Feedback history</DialogTitle>
          <DialogDescription>
            Feedback you have received so far.
          </DialogDescription>
        </DialogHeader>
        {groups.length === 0 ? (
          <p className="py-6 text-center text-sm text-muted-foreground">
            No feedback yet.
          </p>
        ) : (
          <div className="max-h-[60vh] min-w-0 overflow-x-hidden overflow-y-auto">
            <div className="flex flex-col gap-4 pr-3">
              {groups.map((group) => (
                <section key={group.round} className="flex flex-col gap-2">
                  <h3 className="text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                    Round {group.round}
                  </h3>
                  {group.entries.map((entry) => (
                    <HistoryEntry key={entry.id} entry={entry} />
                  ))}
                </section>
              ))}
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
