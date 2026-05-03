package nz.coreyh.risktionary.game.web.dto.response

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.web.dto.GameSessionHostView

class JoinGameResponse(
    val session: GameSessionHostView,
    val ticket: String,
    val playerId: GamePlayerId,
    val displayName: String,
)
