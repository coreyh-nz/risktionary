/**
 * Representation of a Kotlin/Java Instant serialized as JSON.
 *
 * Kotlin's Instant is composed of:
 * - epochSeconds: whole seconds since Unix epoch (1970-01-01T00:00:00Z)
 * - nanosecondsOfSecond: fractional second precision (0–999,999,999)
 */
export interface Instant {
  epochSeconds: number
  nanosecondsOfSecond: number
}

/**
 * Converts a Kotlin/Java Instant (epochSeconds + nanoseconds) into a JavaScript Date.
 *
 * - JavaScript Date only supports millisecond precision
 * - Nanoseconds beyond milliseconds are truncated (not rounded)
 * - This is a lossy conversion for sub-millisecond precision
 *
 * @param instant - The Kotlin/Java Instant object
 * @returns JavaScript Date representing the same point in time (to millisecond precision)
 */
export const instantToDate = (instant: Instant): Date => {
  const millis =
    instant.epochSeconds * 1000 +
    Math.floor(instant.nanosecondsOfSecond / 1_000_000)

  return new Date(millis)
}
