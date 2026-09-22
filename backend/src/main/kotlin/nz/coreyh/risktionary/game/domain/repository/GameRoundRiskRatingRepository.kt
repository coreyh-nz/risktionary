package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.game.domain.model.risk.PlayerRiskRating
import nz.coreyh.risktionary.game.domain.model.details.PersistedRiskRating
import nz.coreyh.risktionary.game.domain.model.round.RoundId

interface GameRoundRiskRatingRepository {
    /** Inserts [ratings], which must be in submission order, so re-ratings are kept as separate rows. */
    fun insertAll(
        roundId: RoundId,
        ratings: List<PlayerRiskRating>,
    )

    /** Every rating submitted in [roundId], including replaced ones, in submission order. */
    fun findByRoundId(roundId: RoundId): List<PersistedRiskRating>
}
