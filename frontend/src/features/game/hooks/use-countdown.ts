import { useState, useEffect } from "react"

export function useCountdown(targetMs: number | null): number {
  const [secondsLeft, setSecondsLeft] = useState<number>(() =>
    targetMs ? Math.max(0, Math.ceil((targetMs - Date.now()) / 1000)) : 0
  )

  useEffect(() => {
    if (!targetMs) return

    const tick = () => {
      const remaining = Math.max(0, Math.ceil((targetMs - Date.now()) / 1000))
      setSecondsLeft(remaining)
    }

    tick() // run immediately
    const id = setInterval(tick, 200) // poll at 200 ms for accuracy
    return () => clearInterval(id)
  }, [targetMs])

  return secondsLeft
}
