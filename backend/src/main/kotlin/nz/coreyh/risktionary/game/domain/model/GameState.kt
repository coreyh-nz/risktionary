package nz.coreyh.risktionary.game.domain.model

/**
 * Represents the lifecycle state of a game session.
 */
sealed interface GameState {
    val type: GameStateType

    data object Lobby : GameState {
        override val type = GameStateType.LOBBY
    }

    data class Starting(
        val timeWindow: TimeWindow,
    ) : GameState {
        override val type = GameStateType.STARTING
    }

    data object InProgress : GameState {
        override val type = GameStateType.IN_PROGRESS
    }

    data object Completed : GameState {
        override val type = GameStateType.COMPLETED
    }
}
