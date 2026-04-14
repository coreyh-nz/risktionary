package nz.coreyh.risktionary.game.domain.model

/**
 * Represents the lifecycle state of a game from creation through completion.
 */
enum class GameState {
    /**
     * The game has been created but is not yet ready for players to join.
     * Any required initialization or configuration occurs during this phase.
     */
    INITIALIZING,

    /**
     * The game has been created but is not yet ready to start.
     * Players may join during this phase, but no gameplay actions
     * or turns can occur.
     */
    LOBBY,

    /**
     * All required players have joined and the game is preparing to begin.
     * A countdown or pre-start sequence is in progress, after which the
     * game will transition to IN_PROGRESS.
     */
    STARTING,

    /**
     * The game has officially begun. All required players have joined,
     * and the game is now progressing through its normal turn or round
     * sequence.
     */
    IN_PROGRESS,

    /**
     * The game is temporarily paused. No gameplay actions may occur
     * until the game is resumed, but the session and player states
     * remain intact.
     */
    PAUSED,

    /**
     * The game has concluded. A final outcome has been reached and
     * no further actions or state changes are permitted.
     */
    COMPLETED,
}
