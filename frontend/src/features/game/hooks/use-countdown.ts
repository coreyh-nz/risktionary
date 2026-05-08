import { useState, useEffect } from "react"

export function useCountdown(durationMs: number): number {
  const [secondsLeft, setSecondsLeft] = useState<number>(() =>
    Math.max(0, Math.ceil(durationMs / 1000))
  )

  useEffect(() => {
    if (!durationMs) return
    const endsAt = Date.now() + durationMs

    const tick = () => {
      const remaining = Math.max(0, Math.ceil((endsAt - Date.now()) / 1000))
      setSecondsLeft(remaining)
    }

    tick()
    const id = setInterval(tick, 200)
    return () => clearInterval(id)
  }, [durationMs])

  return secondsLeft
}
