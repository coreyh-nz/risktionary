package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHost
import kotlin.time.Clock
import kotlin.time.Instant

fun createTestGameSession(
    id: GameId = createTestGameId(),
    host: GameSessionHost = createTestGameSessionHostConnected(),
    code: String = "123456",
    config: GameConfiguration = createTestGameConfiguration(),
    clock: Clock = Clock.System,
    createdAt: Instant = clock.now(),
): GameSession =
    GameSession(
        id = id,
        host = host,
        code = code,
        config = config,
        createdAt = createdAt,
        clock = clock,
    )
