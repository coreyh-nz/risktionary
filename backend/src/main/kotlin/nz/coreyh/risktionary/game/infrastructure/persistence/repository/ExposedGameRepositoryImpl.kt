package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.repository.GameRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGamePhaseDurationTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameWordTable
import nz.coreyh.risktionary.user.domain.model.UserId
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import kotlin.time.Instant

@Repository
class ExposedGameRepositoryImpl : GameRepository {
    override fun create(
        id: GameId,
        code: String,
        hostId: UserId,
        createdAt: Instant,
        config: GameConfiguration,
    ) {
        transaction {
            ExposedGameTable.insert {
                it[ExposedGameTable.id] = id.value
                it[ExposedGameTable.code] = code
                it[ExposedGameTable.hostUserId] = hostId.value
                it[ExposedGameTable.createdAt] = createdAt
                it[ExposedGameTable.feedbackGenerationMode] = config.feedbackGenerationMode
                it[ExposedGameTable.lobbyCountdownMs] = config.lobbyCountdown.inWholeMilliseconds
                it[ExposedGameTable.skippingCountdownsEnabled] = config.skippingCountdownsEnabled
            }

            config.phaseDurations.entries
                .takeIf { it.isNotEmpty() }
                ?.let {
                    ExposedGamePhaseDurationTable.batchInsert(it) { (phase, duration) ->
                        this[ExposedGamePhaseDurationTable.gameId] = id.value
                        this[ExposedGamePhaseDurationTable.phase] = phase
                        this[ExposedGamePhaseDurationTable.durationMs] = duration.inWholeMilliseconds
                    }
                }

            config.words
                .withIndex()
                .toList()
                .takeIf { it.isNotEmpty() }
                ?.let {
                    ExposedGameWordTable.batchInsert(it) { (index, word) ->
                        this[ExposedGameWordTable.gameId] = id.value
                        this[ExposedGameWordTable.position] = index
                        this[ExposedGameWordTable.wordId] = word.id.value
                        this[ExposedGameWordTable.wordValue] = word.value
                    }
                }
        }
    }

    override fun markEnded(
        id: GameId,
        reason: GameEndReason,
        endedAt: Instant,
    ) {
        transaction {
            val count =
                ExposedGameTable.update(where = { ExposedGameTable.id eq id.value }) {
                    it[ExposedGameTable.endReason] = reason
                    it[ExposedGameTable.endedAt] = endedAt
                }
            check(count == 1) { "Expected to mark exactly one game ended but updated $count (game=$id)" }
        }
    }
}
