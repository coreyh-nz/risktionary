package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.domain.model.TimeWindow
import kotlin.time.Clock

data class TimerView(
    val durationMs: Long,
    val remainingMs: Long,
)

fun TimeWindow.toTimerView(clock: Clock = Clock.System): TimerView =
    TimerView(
        durationMs = duration.inWholeMilliseconds,
        remainingMs = remainingMs(clock) ?: 0,
    )
