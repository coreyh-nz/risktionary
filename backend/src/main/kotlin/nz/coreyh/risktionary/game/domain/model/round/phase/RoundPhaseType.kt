package nz.coreyh.risktionary.game.domain.model.round.phase

/**
 * Represents the phases within a single round of the game. A round
 * typically revolves around one word/risk being drawn, guessed, ranked,
 * and discussed.
 */
enum class RoundPhaseType {
    INITIALISING,

    /**
     * Active drawing phase. One player is drawing the risk, others are
     * guessing. Timer is usually running here.
     */
    DRAWING,

    /**
     * Drawing review phase. The correct answer shown.
     */
    DRAWING_REVIEW,

    /**
     * Players individually rate/rank the risk across multiple attributes
     * (likelihood, impact, mitigation difficulty, etc.).
     */
    RANKING,

    /**
     * Results of the ranking phase are shown (heat map, averages, player
     * distribution, etc.). Everyone can see how the group perceived the risk.
     */
    RANKING_REVIEW,

    /**
     * Detailed explanation / educational review of the actual risk. Usually
     * includes the real definition, consequences, examples, etc. Ideal phase
     * for the teacher/facilitator to lead discussion.
     */
    WORD_REVIEW,

    /**
     * Final scoring and summary for the completed round. Displays total round
     * points, updates overall game leaderboard, shows standout performances,
     * and prepares the game for the next round. This acts as a satisfying
     * conclusion to the round before moving on.
     */
    SCORING,

    /**
     * Round has finished. Ready for transition to the next round or game
     * completion.
     */
    COMPLETED,
}
