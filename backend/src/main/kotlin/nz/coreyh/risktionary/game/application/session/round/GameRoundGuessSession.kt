package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import java.util.concurrent.ConcurrentHashMap

class GameRoundGuessSession : LockableSession() {
    private val guesses: MutableMap<GamePlayerId, MutableList<GameRoundGuess>> = mutableMapOf()
    private val correctGuesserIds: MutableSet<GamePlayerId> = ConcurrentHashMap.newKeySet()

    fun recordGuess(
        playerId: GamePlayerId,
        text: String,
        result: GuessResultType,
    ) {
        withLock {
            guesses.getOrPut(playerId) { mutableListOf() }.add(
                GameRoundGuess(
                    playerId = playerId,
                    text = text,
                    result = result,
                ),
            )
        }
        if (result == GuessResultType.CORRECT) {
            correctGuesserIds.add(playerId)
        }
    }

    fun hasAllGuessedCorrectly(playerIds: Collection<GamePlayerId>): Boolean = correctGuesserIds.containsAll(playerIds)

    fun hasGuessedCorrectly(playerId: GamePlayerId): Boolean = correctGuesserIds.contains(playerId)

    fun getCorrectGuessCount() = guesses.size
}
