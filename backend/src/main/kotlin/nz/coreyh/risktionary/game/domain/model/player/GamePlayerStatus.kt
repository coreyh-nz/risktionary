package nz.coreyh.risktionary.game.domain.model.player

/**
 * Represents the lifecycle state of a player within a game session.
 */
enum class GamePlayerStatus {
    /**
     * The player has been authorized to join the game but has not yet
     * established the WebSocket connection required for active participation.
     */
    PENDING,

    CONNECTING,

    /**
     * The player has successfully connected via WebSocket and is an active,
     * present participant in the game session.
     */
    ACTIVE,

    /**
     * The player was previously active but has lost their WebSocket connection.
     * This state represents a temporary interruption, and the player may
     * reconnect and return to ACTIVE.
     */
    DISCONNECTED,

    /**
     * The player has intentionally left the game session. Unlike DISCONNECTED,
     * this state is final and the player is not expected to return.
     */
    QUIT,
}
