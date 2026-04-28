package nz.coreyh.risktionary.game.domain.model.player

import nz.coreyh.risktionary.game.domain.model.GameId
import kotlin.time.Instant

data class GameTicket(
    val gameId: GameId,
    val playerId: GamePlayerId,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val value: String,
)
