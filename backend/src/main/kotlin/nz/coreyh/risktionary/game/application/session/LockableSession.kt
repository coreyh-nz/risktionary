package nz.coreyh.risktionary.game.application.session

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Base class for session objects that require thread-safe access to mutable state.
 *
 * A single [ReentrantLock] is used to guard all state mutations and reads performed
 * through [withLock]. This ensures that concurrent events, such as WebSocket lifecycle
 * callbacks or asynchronous application operations, cannot mutate session state
 * simultaneously.
 *
 * Subclasses should perform all access to shared mutable state through [withLock].
 */
abstract class LockableSession {
    protected val lock = ReentrantLock()

    /**
     * Executes the given block while holding the session lock.
     *
     * This helper provides mutually exclusive access to session state and should be
     * used for all reads and mutations of shared mutable data.
     */
    protected inline fun <T> withLock(block: () -> T): T = lock.withLock(block)
}
