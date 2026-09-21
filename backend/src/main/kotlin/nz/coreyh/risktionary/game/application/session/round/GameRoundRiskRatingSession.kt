package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.risk.PlayerRiskRating
import nz.coreyh.risktionary.game.domain.model.risk.RiskLikelihood
import nz.coreyh.risktionary.game.domain.model.risk.RiskRating
import nz.coreyh.risktionary.game.domain.model.risk.RiskRatingCount
import nz.coreyh.risktionary.game.domain.model.risk.RiskSeverity
import kotlin.time.Clock

/**
 * Tracks each player's risk rating (likelihood and severity) for the
 * round's word.
 */
class GameRoundRiskRatingSession(
    private val clock: Clock = Clock.System,
) : LockableSession() {
    private val ratings: MutableMap<GamePlayerId, PlayerRiskRating> = mutableMapOf()
    private val history: MutableList<PlayerRiskRating> = mutableListOf()

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
                    ratedAt = clock.now(),
                )
            ratings[playerId] = rating
            history.add(rating)
            rating
        }

    fun getRatings(): List<PlayerRiskRating> = withLock { ratings.values.toList() }

    /** Every rating ever submitted, in submission order, including ones since replaced. */
    fun getRatingHistory(): List<PlayerRiskRating> = withLock { history.toList() }

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
