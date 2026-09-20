package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.domain.model.round.guess.createGuessId
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Instant

class GameRoundGuessSession(
    private val clock: Clock = Clock.System,
    private val elapsedMsAt: (Instant) -> Long? = { null },
) : LockableSession() {
    private val guesses: MutableList<GameRoundGuess> = mutableListOf()
    private val correctGuesserIds: MutableSet<GamePlayerId> = ConcurrentHashMap.newKeySet()

    fun recordGuess(
        playerId: GamePlayerId,
        text: String,
        result: GuessResultType,
    ): GameRoundGuess {
        val now = clock.now()
        val guess =
            GameRoundGuess(
                id = createGuessId(),
                playerId = playerId,
                text = text,
                result = result,
                submittedAt = now,
                elapsedMs = elapsedMsAt(now),
            )
        withLock { guesses.add(guess) }
        if (result == GuessResultType.CORRECT) {
            correctGuesserIds.add(playerId)
        }
        return guess
    }

    /** Every guess in the order it was submitted. */
    fun getGuesses(): List<GameRoundGuess> = withLock { guesses.toList() }

    fun hasAllGuessedCorrectly(playerIds: Collection<GamePlayerId>): Boolean = correctGuesserIds.containsAll(playerIds)

    fun hasGuessedCorrectly(playerId: GamePlayerId): Boolean = correctGuesserIds.contains(playerId)

    fun getCorrectGuessCount() = correctGuesserIds.size
}
