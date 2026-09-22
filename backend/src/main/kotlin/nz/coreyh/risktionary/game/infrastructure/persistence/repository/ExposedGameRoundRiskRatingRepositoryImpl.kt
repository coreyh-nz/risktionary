package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import java.util.UUID
import nz.coreyh.risktionary.game.domain.model.details.PersistedRiskRating
import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerId
import nz.coreyh.risktionary.game.domain.model.risk.PlayerRiskRating
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.repository.GameRoundRiskRatingRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundRiskRatingTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGameRoundRiskRatingRepositoryImpl : GameRoundRiskRatingRepository {
    override fun insertAll(
        roundId: RoundId,
        ratings: List<PlayerRiskRating>,
    ) {
        if (ratings.isEmpty()) return
        transaction {
            // seq counts per player, so the highest seq for a player is their final rating
            val nextSeq = mutableMapOf<UUID, Int>()
            ExposedGameRoundRiskRatingTable.batchInsert(ratings) { rating ->
                val playerId = rating.playerId.value
                val seq = nextSeq.getOrDefault(playerId, 0)
                nextSeq[playerId] = seq + 1
                this[ExposedGameRoundRiskRatingTable.roundId] = roundId.value
                this[ExposedGameRoundRiskRatingTable.playerId] = playerId
                this[ExposedGameRoundRiskRatingTable.seq] = seq
                this[ExposedGameRoundRiskRatingTable.likelihood] = rating.rating.likelihood
                this[ExposedGameRoundRiskRatingTable.severity] = rating.rating.severity
                this[ExposedGameRoundRiskRatingTable.ratedAt] = rating.ratedAt
            }
        }
    }

    override fun findByRoundId(roundId: RoundId): List<PersistedRiskRating> =
        transaction {
            ExposedGameRoundRiskRatingTable
                .selectAll()
                .where { ExposedGameRoundRiskRatingTable.roundId eq roundId.value }
                .orderBy(ExposedGameRoundRiskRatingTable.seq, SortOrder.ASC)
                .map {
                    PersistedRiskRating(
                        playerId = it[ExposedGameRoundRiskRatingTable.playerId].toGamePlayerId(),
                        seq = it[ExposedGameRoundRiskRatingTable.seq],
                        likelihood = it[ExposedGameRoundRiskRatingTable.likelihood],
                        severity = it[ExposedGameRoundRiskRatingTable.severity],
                        ratedAt = it[ExposedGameRoundRiskRatingTable.ratedAt],
                    )
                }
        }
}
