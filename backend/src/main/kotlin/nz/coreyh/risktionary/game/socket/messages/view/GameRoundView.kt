package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.application.session.round.GameRoundSession

data class GameRoundView(
    val number: Int,
    val state: GameRoundStateView,
)

fun GameRoundSession.toView(): GameRoundView =
    GameRoundView(
        number = game.roundNumber,
        state = toStateView(),
    )
