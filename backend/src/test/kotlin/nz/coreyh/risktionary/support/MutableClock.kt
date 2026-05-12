package nz.coreyh.risktionary.support

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class MutableClock(
    private var instant: Instant = Instant.fromEpochSeconds(0),
) : Clock {
    override fun now(): Instant = instant

    fun advance(duration: Duration) {
        instant += duration
    }
}
