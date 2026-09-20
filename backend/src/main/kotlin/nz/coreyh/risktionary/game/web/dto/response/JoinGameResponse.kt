package nz.coreyh.risktionary.game.web.dto.response

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.web.dto.GameSessionHostView

class JoinGameResponse(
    val session: GameSessionHostView,
    val ticket: String,
    val playerId: GamePlayerId,
    val displayName: String,
    /** Whether AI feedback is generated in this game. When false, the client should not show any feedback UI. */
    val feedbackEnabled: Boolean,
)
