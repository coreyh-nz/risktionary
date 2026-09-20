package nz.coreyh.risktionary.ai.infrastructure.dispatch

import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * A bounded, closeable async execution context for AI-related I/O calls.
 */
@OptIn(ExperimentalAtomicApi::class)
class AiDispatcher(
    parallelism: Int = 2,
) : AutoCloseable {
    private val executor =
        Executors.newFixedThreadPool(parallelism) {
            Thread(it, "ai-dispatcher-${threadCounter.fetchAndAdd(1)}")
        }
    private val dispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    /**
     * Runs [block] on the dispatcher, then passes its result to [onResult] or
     * its failure to [onError]. Returns the running job so callers can wait
     * for it or cancel it.
     */
    fun <T> launch(
        block: suspend () -> T,
        onResult: (T) -> Unit,
        onError: (Throwable) -> Unit = {},
    ): Job =
        scope.launch {
            try {
                onResult(block())
            } catch (e: Throwable) {
                onError(e)
            }
        }

    override fun close() {
        scope.cancel()
        executor.shutdown()
    }

    companion object {
        private val threadCounter = AtomicInt(1)
    }
}
