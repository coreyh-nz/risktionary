package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.game.domain.model.round.GameRoundAiUsage
import nz.coreyh.risktionary.game.domain.model.round.RoundId

interface GameRoundAiUsageRepository {
    /** Inserts [usage], which must be in the order it was recorded. */
    fun insertAll(
        roundId: RoundId,
        usage: List<GameRoundAiUsage>,
    )

    /** Every AI call recorded in [roundId], in the order it was recorded. */
    fun findByRoundId(roundId: RoundId): List<GameRoundAiUsage>
}
