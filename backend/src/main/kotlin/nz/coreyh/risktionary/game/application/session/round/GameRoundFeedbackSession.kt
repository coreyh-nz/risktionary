package nz.coreyh.risktionary.game.application.session.round

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
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
    private val pendingJobs = CopyOnWriteArrayList<Job>()

    /**
     * True once the round has stopped waiting for pending work. Anything that
     * finishes after this point is too late to be recorded.
     */
    @Volatile
    var isSettled: Boolean = false
        private set

    /** Registers a running generation, so the round can wait for it before it is saved. */
    fun track(job: Job) {
        pendingJobs.add(job)
    }

    /**
     * Blocks until every tracked generation has finished, or [timeout] has
     * passed. Anything still running after that is cancelled and the round is
     * marked settled so late results are discarded.
     *
     * @return how many generations were abandoned because they did not finish in time.
     */
    fun awaitPending(timeout: Duration): Int {
        val pending = pendingJobs.filter { it.isActive }
        if (pending.isNotEmpty()) {
            runBlocking { withTimeoutOrNull(timeout) { pending.joinAll() } }
        }
        isSettled = true
        val abandoned = pending.filter { it.isActive }
        abandoned.forEach { it.cancel() }
        return abandoned.size
    }

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
