import { durationToTimePoint } from "@/lib/time"
import { CountdownState, TimerView } from "@/types/time"

export const mapTimerViewToCountdown = (timer: TimerView): CountdownState => {
  return {
    durationMs: timer.durationMs,
    endsAt: durationToTimePoint(timer.remainingMs),
  }
}
