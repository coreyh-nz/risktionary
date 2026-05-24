package nz.coreyh.risktionary.game.socket.messages.event

/**
 * Defines all possible types of game socket events.
 */
enum class GameEventType {
    // game events
    STATE_CHANGED,
    VOLUNTEERS_UPDATED,

    // player events
    PLAYER_JOINED,
    PLAYER_LEFT,
    PLAYER_LIST_UPDATED,
}
