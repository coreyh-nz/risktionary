package nz.coreyh.risktionary.game.web.dto.response

import nz.coreyh.risktionary.game.web.dto.GameSessionHostView

data class CreateGameResponse(
    val session: GameSessionHostView,
)
