package nz.coreyh.risktionary.game.domain.model.host

import kotlin.time.Instant

sealed interface GameSessionHostStatus {
    data object Pending : GameSessionHostStatus

    data class Connected(
        val connectedAt: Instant,
    ) : GameSessionHostStatus

    data class Disconnected(
        val disconnectedAt: Instant,
    ) : GameSessionHostStatus
}
