package nz.coreyh.risktionary.ai.infrastructure.dispatch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * A bounded, closeable async execution context for AI-related I/O calls.
 */
class AiDispatcher(
    parallelism: Int = 2,
) : AutoCloseable {
    private val executor = Executors.newFixedThreadPool(parallelism)
    private val dispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    fun <T> launch(
        block: suspend () -> T,
        onResult: (T) -> Unit,
        onError: (Throwable) -> Unit = {},
    ) {
        scope.launch {
            try {
                onResult(block())
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    override fun close() {
        scope.cancel()
        executor.shutdown()
    }
}
