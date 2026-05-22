package nz.coreyh.risktionary.game.socket.messages.event.round

/**
 * Base class for all round-related socket events.
 *
 * Each event carries a [RoundEventType] which is used by the client/server
 * to determine how the event should be handled.
 */
abstract class RoundEvent(
    val type: RoundEventType,
)
