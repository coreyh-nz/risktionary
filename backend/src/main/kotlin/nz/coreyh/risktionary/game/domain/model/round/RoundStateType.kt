package nz.coreyh.risktionary.game.domain.model.round

/**
 * Represents the top-level lifecycle state of a single round.
 */
enum class RoundStateType {
    /**
     * Players are volunteering to draw, and the host is selecting who will draw.
     * No drawer has been confirmed yet.
     */
    SELECTING_DRAWER,

    /**
     * A drawer has been confirmed and the round is actively progressing through
     * its phases.
     *
     * The specific phase within this state is communicated separately
     * via [nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType].
     */
    IN_PROGRESS,

    /**
     * The round has fully concluded. Scores have been tallied and awarded.
     * The game is ready to transition to the next round or end.
     */
    COMPLETED,
}
