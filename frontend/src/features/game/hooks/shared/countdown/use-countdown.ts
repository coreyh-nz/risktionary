import { timeUntil } from "@/lib/time"
import { CountdownState } from "@/types/time"
import { useEffect, useState } from "react"

export function useCountdown(countdown: CountdownState): {
  secondsLeft: number
  totalSeconds: number
}

// covers the (CountdownState | undefined) case (never shouldn't be passing in a known undefined)
export function useCountdown(countdown?: CountdownState): {
  secondsLeft: number | undefined
  totalSeconds: number | undefined
}

export function useCountdown(countdown?: CountdownState) {
  const [secondsLeft, setSecondsLeft] = useState<number | undefined>(() =>
    countdown ? Math.ceil(timeUntil(countdown.endsAt) / 1000) : undefined
  )

  useEffect(() => {
    if (!countdown) return

    const tick = () => {
      const next = Math.ceil(timeUntil(countdown.endsAt) / 1000)
      setSecondsLeft((prev) => (prev === next ? prev : next))
    }

    tick()

    const interval = setInterval(tick, 100)
    return () => clearInterval(interval)
  }, [countdown])

  return {
    secondsLeft,
    totalSeconds:
      countdown == null ? undefined : Math.ceil(countdown.durationMs / 1000),
  }
}
