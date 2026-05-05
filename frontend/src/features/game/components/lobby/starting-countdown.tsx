"use client"
import { useState } from "react"
import { useCountdown } from "@/features/game/hooks/use-countdown"
import { CountdownRing } from "@/features/game/components/countdown/countdown-ring"
import { CountdownDigit } from "@/features/game/components/countdown/countdown-digit"

interface GameStartingCountdownProps {
  startsAt: Date
  isHost?: boolean
}

export const GameStartingCountdown = ({
  startsAt,
  isHost = false,
}: GameStartingCountdownProps) => {
  const secondsLeft = useCountdown(startsAt.getTime())
  const [total] = useState(() =>
    Math.max(1, Math.ceil((startsAt.getTime() - Date.now()) / 1000))
  )

  return isHost ? (
    <HostCountdown secondsLeft={secondsLeft} total={total} />
  ) : (
    <PlayerCountdown secondsLeft={secondsLeft} total={total} />
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
