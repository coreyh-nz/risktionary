package nz.coreyh.risktionary.game.domain.model.round.guess

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

data class GameRoundGuess(
    val playerId: GamePlayerId,
    val text: String,
    val result: GuessResultType,
)
