package nz.coreyh.risktionary.game.web.dto.response

import nz.coreyh.risktionary.game.domain.model.GameId

data class CreateGameResponse(
    val gameId: GameId,
)
