package nz.coreyh.risktionary.game.web.dto

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.GameState

data class GameSessionHostView(
    val id: GameId,
    val code: String,
    val state: GameState,
)

data class GameSessionPlayerView(
    val code: String,
    val state: GameState,
)

fun GameSession.toHostView(): GameSessionHostView =
    GameSessionHostView(
        id = id,
        code = code,
        state = state,
    )

fun GameSession.toPlayerView(): GameSessionPlayerView =
    GameSessionPlayerView(
        code = code,
        state = state,
    )
