package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

fun createTestGameTicket(
    gameId: GameId = createTestGameId(),
    playerId: GamePlayerId = createTestGamePlayerId(),
    issuedAt: Instant = Clock.System.now(),
    expiresAt: Instant = issuedAt + 30.minutes,
    value: String = "ticket",
): GameTicket =
    GameTicket(
        gameId,
        playerId,
        issuedAt,
        expiresAt,
        value,
    )
