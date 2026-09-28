"use client"

import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog"
import {
  useGameIsHost,
  useGameRoundResult,
  useGameSetRoundResult,
} from "@/features/game/stores/game-store-selectors"
import { cn } from "cn"
import confetti from "canvas-confetti"
import { Clock, PartyPopper } from "lucide-react"
import { useEffect } from "react"

const AUTO_CLOSE_MS = 5000

export const RoundResultModal = () => {
  const isHost = useGameIsHost()
  const result = useGameRoundResult()
  const setRoundResult = useGameSetRoundResult()

  const open = !isHost && result !== null

  useEffect(() => {
    if (!open) return

    const timeout = setTimeout(() => setRoundResult(null), AUTO_CLOSE_MS)
    return () => clearTimeout(timeout)
  }, [open, setRoundResult])

  useEffect(() => {
    if (isHost || result?.type !== "CORRECT") return

    confetti({
      particleCount: 120,
      spread: 70,
      origin: { y: 0.6 },
    })
  }, [isHost, result])

  if (!open || !result) return null

  const isCorrect = result.type === "CORRECT"

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!next) setRoundResult(null)
      }}
    >
      <DialogContent
        showCloseButton={false}
        className="flex flex-col items-center gap-3 py-8 text-center"
      >
        <div
          className={cn(
            "flex size-16 items-center justify-center rounded-full animate-pop",
            isCorrect
              ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
              : "bg-blue-500/10 text-blue-600 dark:text-blue-400"
          )}
        >
          {isCorrect ? (
            <PartyPopper className="size-7" />
          ) : (
            <Clock className="size-7" />
          )}
        </div>

        <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">
          {isCorrect ? "Nice work" : "Round over"}
        </p>

        <DialogTitle
          className={cn(
            "text-2xl font-bold",
            isCorrect
              ? "text-emerald-600 dark:text-emerald-400"
              : "text-foreground"
          )}
        >
          {isCorrect ? "Correct!" : "Time's up!"}
        </DialogTitle>

        <p className="text-sm text-muted-foreground">
          {isCorrect
            ? "You guessed the risk before the timer ran out."
            : "You didn't guess the risk in time."}
        </p>

        {isCorrect && (
          <Badge
            variant="secondary"
            className="h-auto gap-1.5 rounded-full px-3 py-1.5 text-sm"
          >
            <span className="font-semibold text-emerald-600 dark:text-emerald-400">
              +{result.points.toLocaleString()} pts
            </span>
          </Badge>
        )}
      </DialogContent>
    </Dialog>
  )
}
