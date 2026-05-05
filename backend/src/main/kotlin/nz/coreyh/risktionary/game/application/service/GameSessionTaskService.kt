package nz.coreyh.risktionary.game.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import nz.coreyh.risktionary.game.domain.model.GameId
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ScheduledFuture
import kotlin.time.Instant
import kotlin.time.toJavaInstant

private val kLogger = KotlinLogging.logger {}

/**
 * Service responsible for scheduling and managing time-based tasks for individual game sessions.
 *
 * This service is primarily used to handle automatic phase transitions, round timers,
 * countdowns, and other time-sensitive operations within a game.
 */
@Service
class GameSessionTaskService(
    private val taskScheduler: TaskScheduler,
) {
    private val tasks = ConcurrentHashMap<GameId, MutableSet<ScheduledFuture<*>>>()

    /**
     * Schedules a task to be executed at a specific future time for a given game.
     *
     * The scheduled future is registered via a [CompletableFuture] before the task
     * body can reference it, ensuring the future is always resolvable even if the
     * scheduler executes the task immediately upon scheduling.
     *
     * @param gameId The ID of the game this task belongs to.
     * @param at The instant when the task should be executed.
     * @param block The action to perform when the scheduled time is reached.
     */
    fun schedule(
        gameId: GameId,
        at: Instant,
        block: () -> Unit,
    ) {
        val gameTasks = tasks.computeIfAbsent(gameId) { CopyOnWriteArraySet() }
        val futureRef = CompletableFuture<ScheduledFuture<*>>()
        val scheduled =
            taskScheduler.schedule({
                try {
                    block()
                } catch (e: Exception) {
                    kLogger.error(e) { "Error in scheduled task for game $gameId" }
                } finally {
                    try {
                        val future = futureRef.get()
                        gameTasks.remove(future)
                        tasks.computeIfPresent(gameId) { _, set ->
                            set.takeIf { it.isNotEmpty() }
                        }
                    } catch (e: Exception) {
                        kLogger.warn(e) { "Failed to clean up task reference for game $gameId" }
                    }
                }
            }, at.toJavaInstant())

        futureRef.complete(scheduled)
        gameTasks.add(scheduled)
    }

    /**
     * Cancels all scheduled tasks for a specific game.
     *
     * This should be called when a game ends, is canceled, or when all timers
     * need to be cleared (e.g., during a full game reset).
     *
     * @param gameId The ID of the game whose tasks should be canceled.
     */
    fun cancelAll(gameId: GameId) {
        tasks.remove(gameId)?.forEach { it.cancel(false) }
    }

    @PreDestroy
    fun shutdown() {
        tasks.keys.forEach { cancelAll(it) }
        tasks.clear()
    }
}
