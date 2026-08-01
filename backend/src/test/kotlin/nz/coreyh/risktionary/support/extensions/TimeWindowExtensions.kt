package nz.coreyh.risktionary.support.extensions

import nz.coreyh.risktionary.game.domain.model.TimeWindow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

fun Duration.timeWindow(startedAt: Instant = Clock.System.now()): TimeWindow =
    TimeWindow(
        startedAt = startedAt,
        duration = this,
    )

fun Duration.timeWindowFromNow(clock: Clock = Clock.System): TimeWindow = timeWindow(startedAt = clock.now())
