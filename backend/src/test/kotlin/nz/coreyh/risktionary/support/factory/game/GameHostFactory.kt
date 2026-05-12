package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.domain.model.host.GameSessionHost
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHostStatus
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant

fun createTestGameSessionHostPending(id: UserId = createTestUserId()): GameSessionHost =
    createTestGameSessionHost(id = id, status = GameSessionHostStatus.Pending)

fun createTestGameSessionHostConnected(
    id: UserId = createTestUserId(),
    connectedAt: Instant = Clock.System.now(),
): GameSessionHost = createTestGameSessionHost(id = id, status = GameSessionHostStatus.Connected(connectedAt))

fun createTestGameSessionHostDisconnected(
    id: UserId = createTestUserId(),
    disconnectedAt: Instant = Clock.System.now(),
): GameSessionHost = createTestGameSessionHost(id = id, status = GameSessionHostStatus.Disconnected(disconnectedAt))

fun createTestGameSessionHost(
    id: UserId = createTestUserId(),
    status: GameSessionHostStatus,
): GameSessionHost =
    GameSessionHost(
        id = id,
        status = status,
    )
