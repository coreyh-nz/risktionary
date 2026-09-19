package nz.coreyh.risktionary.game.application.session.round

import java.util.concurrent.CopyOnWriteArrayList
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGuessContext
import nz.coreyh.risktionary.feedback.domain.model.GeneratedFeedback
import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

/**
 * Holds the feedback state of a single round: each player's guesses with the
 * context they were made in (the input to fact generation), and the feedback
 * generated from them.
 */
class GameRoundFeedbackSession : LockableSession() {
    private val guessesByPlayer = mutableMapOf<GamePlayerId, MutableList<FeedbackGuessContext>>()
    private val feedback = CopyOnWriteArrayList<GeneratedFeedback>()

    /**
     * Adds [guess] to [playerId]'s guesses and returns all of their guesses
     * so far, oldest first, including this one.
     */
    fun addGuess(
        playerId: GamePlayerId,
        guess: FeedbackGuessContext,
    ): List<FeedbackGuessContext> =
        withLock {
            val guesses = guessesByPlayer.getOrPut(playerId) { mutableListOf() }
            guesses.add(guess)
            guesses.toList()
        }

    /** Every player's guesses so far, oldest first. */
    fun getGuessesByPlayer(): Map<GamePlayerId, List<FeedbackGuessContext>> =
        withLock { guessesByPlayer.mapValues { (_, guesses) -> guesses.toList() } }

    fun record(generated: GeneratedFeedback) {
        feedback.add(generated)
    }

    /** Every piece of feedback generated this round, in the order it completed. */
    fun getAll(): List<GeneratedFeedback> = feedback.toList()

    fun getFor(playerId: GamePlayerId): List<GeneratedFeedback> = feedback.filter { it.playerId == playerId }
}
