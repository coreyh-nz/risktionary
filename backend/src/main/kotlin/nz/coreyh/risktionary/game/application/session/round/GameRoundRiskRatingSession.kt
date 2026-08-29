package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.risk.PlayerRiskRating
import nz.coreyh.risktionary.game.domain.model.risk.RiskLikelihood
import nz.coreyh.risktionary.game.domain.model.risk.RiskRating
import nz.coreyh.risktionary.game.domain.model.risk.RiskRatingCount
import nz.coreyh.risktionary.game.domain.model.risk.RiskSeverity

/**
 * Tracks each player's risk rating (likelihood and severity) for the
 * round's word.
 */
class GameRoundRiskRatingSession : LockableSession() {
    private val ratings: MutableMap<GamePlayerId, PlayerRiskRating> = mutableMapOf()

    /**
     * Records or replaces [playerId]'s rating.
     */
    fun submitRating(
        playerId: GamePlayerId,
        likelihood: RiskLikelihood,
        severity: RiskSeverity,
    ): PlayerRiskRating =
        withLock {
            val rating =
                PlayerRiskRating(
                    playerId = playerId,
                    rating = RiskRating(likelihood = likelihood, severity = severity),
                )
            ratings[playerId] = rating
            rating
        }

    fun getRatings(): List<PlayerRiskRating> = withLock { ratings.values.toList() }

    fun hasRated(playerId: GamePlayerId): Boolean = withLock { ratings.contains(playerId) }

    /**
     * Groups ratings by (likelihood, severity) and counts how many players
     * fall into each cell.
     */
    fun counts(): List<RiskRatingCount> =
        withLock {
            ratings.values
                .groupBy { it.rating.likelihood to it.rating.severity }
                .map { (cell, group) ->
                    RiskRatingCount(
                        likelihood = cell.first,
                        severity = cell.second,
                        count = group.size,
                    )
                }
        }
}
