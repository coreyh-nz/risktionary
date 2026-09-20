package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.game.domain.model.risk.PlayerRiskRating
import nz.coreyh.risktionary.game.domain.model.round.RoundId

interface GameRoundRiskRatingRepository {
    /** Inserts [ratings], which must be in submission order, so re-ratings are kept as separate rows. */
    fun insertAll(
        roundId: RoundId,
        ratings: List<PlayerRiskRating>,
    )
}
