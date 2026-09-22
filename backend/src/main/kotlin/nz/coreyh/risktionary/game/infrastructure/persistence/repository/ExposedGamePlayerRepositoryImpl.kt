package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.details.PersistedGamePlayer
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerId
import nz.coreyh.risktionary.game.domain.repository.GamePlayerRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGamePlayerTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGamePlayerRepositoryImpl : GamePlayerRepository {
    override fun insertMissing(
        gameId: GameId,
        players: Map<GamePlayerId, GamePlayerFeedbackAssignment?>,
    ) {
        if (players.isEmpty()) return
        transaction {
            val existingIds =
                ExposedGamePlayerTable
                    .select(ExposedGamePlayerTable.id)
                    .where { ExposedGamePlayerTable.gameId eq gameId.value }
                    .map { it[ExposedGamePlayerTable.id].value }
                    .toSet()
            val missing = players.entries.filter { it.key.value !in existingIds }
            if (missing.isEmpty()) return@transaction

            ExposedGamePlayerTable.batchInsert(missing) { (playerId, assignment) ->
                this[ExposedGamePlayerTable.id] = playerId.value
                this[ExposedGamePlayerTable.gameId] = gameId.value
                this[ExposedGamePlayerTable.feedbackFramingCondition] = assignment?.framingCondition
                this[ExposedGamePlayerTable.feedbackTimingCondition] = assignment?.timingCondition
            }
        }
    }

    override fun findByGameId(gameId: GameId): List<PersistedGamePlayer> =
        transaction {
            ExposedGamePlayerTable
                .selectAll()
                .where { ExposedGamePlayerTable.gameId eq gameId.value }
                .map {
                    PersistedGamePlayer(
                        id = it[ExposedGamePlayerTable.id].value.toGamePlayerId(),
                        feedbackFramingCondition = it[ExposedGamePlayerTable.feedbackFramingCondition],
                        feedbackTimingCondition = it[ExposedGamePlayerTable.feedbackTimingCondition],
                    )
                }
        }
}
