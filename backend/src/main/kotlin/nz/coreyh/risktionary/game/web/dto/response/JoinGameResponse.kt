package nz.coreyh.risktionary.game.web.dto.response

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.web.dto.GameSessionPlayerView

class JoinGameResponse(
    val session: GameSessionPlayerView,
    val ticket: String,
    val playerId: GamePlayerId,
    val displayName: String,
)
