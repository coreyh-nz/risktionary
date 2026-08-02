"use client"

import { CountdownDigit } from "@/features/game/components/shared/countdown/countdown-digit"
import { CountdownRing } from "@/features/game/components/shared/countdown/countdown-ring"
import { CountdownState } from "@/types/time"
import { useCountdown } from "../../hooks/shared/countdown/use-countdown"

interface GameStartingCountdownProps {
  countdown: CountdownState
  isHost?: boolean
}

export const GameStartingCountdown = ({
  countdown,
  isHost = false,
}: GameStartingCountdownProps) => {
  const { secondsLeft, totalSeconds } = useCountdown(countdown)
  if (secondsLeft === undefined || totalSeconds === undefined) return

  return isHost ? (
    <HostCountdown secondsLeft={secondsLeft} total={totalSeconds} />
  ) : (
    <PlayerCountdown secondsLeft={secondsLeft} total={totalSeconds} />
  )
}

const PlayerCountdown = ({
  secondsLeft,
  total,
}: {
  secondsLeft: number
  total: number
}) => (
  <div className="fixed inset-0 z-40 flex flex-col items-center justify-center gap-16 bg-background/95 backdrop-blur-sm">
    <div className="relative flex items-center justify-center">
      <CountdownRing secondsLeft={secondsLeft} total={total} size="xl" />
      <CountdownDigit
        secondsLeft={secondsLeft}
        size="xl"
        className="absolute"
      />
    </div>
    <div className="flex flex-col items-center gap-1 text-center">
      <p className="text-2xl font-bold tracking-tight text-foreground">
        Get Ready!
      </p>
      <p className="text-sm text-muted-foreground">
        The host is starting the game…
      </p>
    </div>
  </div>
)

const HostCountdown = ({
  secondsLeft,
  total,
}: {
  secondsLeft: number
  total: number
}) => (
  <div className="fixed right-0 bottom-0 left-0 z-40 flex items-center justify-between gap-8 border-t bg-background px-12 py-6 shadow-2xl">
    <p className="text-3xl font-black tracking-tight text-foreground">
      Game is starting!
    </p>

    <div className="flex items-center gap-6">
      <div className="flex flex-col items-end">
        <p className="text-xs font-semibold tracking-widest text-muted-foreground uppercase">
          Starting in
        </p>
        <CountdownDigit secondsLeft={secondsLeft} size="lg" />
      </div>
      <CountdownRing secondsLeft={secondsLeft} total={total} size="md" />
    </div>
  </div>
)
