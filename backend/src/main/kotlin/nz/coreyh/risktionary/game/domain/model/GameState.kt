package nz.coreyh.risktionary.game.domain.model

import kotlin.time.Instant

/**
 * Represents the lifecycle state of a game session.
 *
 * This abstraction allows the domain to express state-specific behaviour
 * and data in a type-safe way, rather than relying on a single enum with
 * optional fields.
 */
sealed interface GameState {
    val type: GameStateType

    data object Initialising : GameState {
        override val type = GameStateType.INITIALIZING
    }

    data object Lobby : GameState {
        override val type = GameStateType.LOBBY
    }

    data class Starting(
        val startingAt: Instant,
    ) : GameState {
        override val type = GameStateType.STARTING
    }

    data object InProgress : GameState {
        override val type = GameStateType.IN_PROGRESS
    }

    data object Paused : GameState {
        override val type = GameStateType.PAUSED
    }

    data object Completed : GameState {
        override val type = GameStateType.COMPLETED
    }
}
