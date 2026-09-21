package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.feedback.domain.model.GeneratedFeedback
import nz.coreyh.risktionary.game.domain.model.round.RoundId

interface GameRoundFeedbackRepository {
    /**
     * Inserts [feedback] and the links to the guesses each was generated
     * from. The guesses must already be inserted.
     */
    fun insertAll(
        roundId: RoundId,
        feedback: List<GeneratedFeedback>,
    )
}
