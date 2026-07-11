package nz.coreyh.risktionary.game.socket.messages.event.round

/**
 * Defines all possible types of round socket events.
 */
enum class RoundEventType {
    STATE,
    PHASE_STATE,

    ASSIGNED_DRAWER,
    ASSIGNED_GUESSER,

    CHAT_MESSAGE,

    CORRECT_GUESS,
    CORRECT_GUESSES_COUNT,
}
