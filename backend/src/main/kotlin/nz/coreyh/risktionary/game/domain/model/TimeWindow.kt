package nz.coreyh.risktionary.game.domain.model

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class TimeWindow(
    val startedAt: Instant,
    val duration: Duration,
) {
    val endingAt: Instant
        get() = duration.let { startedAt + it }

    fun remainingMs(clock: Clock = Clock.System): Long =
        (endingAt - clock.now())
            .inWholeMilliseconds
            .coerceAtLeast(0)
}
