package nz.coreyh.risktionary.game.socket.messages.event

/**
 * Base class for all game-related socket events.
 *
 * Each event carries a [GameEventType] which is used by the client/server
 * to determine how the event should be handled.
 */
interface GameEvent {
    val type: GameEventType
}
