package nz.coreyh.risktionary.game.domain.model.host

import nz.coreyh.risktionary.user.domain.model.UserId
import kotlin.time.Instant

class GameSessionHost(
    val id: UserId,
    status: GameSessionHostStatus,
) {
    var status: GameSessionHostStatus = status
        private set

    fun connect(connectedAt: Instant) {
        status = GameSessionHostStatus.Connected(connectedAt)
    }

    fun disconnect(disconnectedAt: Instant) {
        status = GameSessionHostStatus.Disconnected(disconnectedAt)
    }
}
