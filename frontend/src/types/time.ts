export interface TimerView {
  durationMs: number
  remainingMs: number
}

export interface CountdownState {
  durationMs: number
  endsAt: number
}
