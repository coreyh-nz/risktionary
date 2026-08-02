/**
 * A point in the monotonic performance clock timeline.
 * Created using performance.now().
 */
export type TimePoint = number

/**
 * Converts a duration from now into a monotonic time point.
 *
 * Example:
 * durationToTimePoint(5000) -> a timestamp 5 seconds from now
 */
export const durationToTimePoint = (durationMs: number): TimePoint => {
  return performance.now() + Math.max(0, durationMs)
}

/**
 * Returns the duration between now and a time point.
 */
export const timeUntil = (timePoint: TimePoint): number => {
  return Math.max(0, timePoint - performance.now())
}
