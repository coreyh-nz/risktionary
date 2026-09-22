package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.GameRoundAiUsage
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.repository.GameRoundAiUsageRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundAiUsageTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGameRoundAiUsageRepositoryImpl : GameRoundAiUsageRepository {
    override fun insertAll(
        roundId: RoundId,
        usage: List<GameRoundAiUsage>,
    ) {
        if (usage.isEmpty()) return
        transaction {
            ExposedGameRoundAiUsageTable.batchInsert(usage.withIndex()) { (index, entry) ->
                this[ExposedGameRoundAiUsageTable.roundId] = roundId.value
                this[ExposedGameRoundAiUsageTable.seq] = index
                this[ExposedGameRoundAiUsageTable.playerId] = entry.playerId?.value
                this[ExposedGameRoundAiUsageTable.usagePurpose] = entry.usage.purpose
                this[ExposedGameRoundAiUsageTable.modelName] = entry.usage.model
                this[ExposedGameRoundAiUsageTable.promptTokens] = entry.usage.promptTokens
                this[ExposedGameRoundAiUsageTable.completionTokens] = entry.usage.completionTokens
                this[ExposedGameRoundAiUsageTable.totalTokens] = entry.usage.totalTokens
                this[ExposedGameRoundAiUsageTable.recordedAt] = entry.recordedAt
            }
        }
    }

    override fun findByRoundId(roundId: RoundId): List<GameRoundAiUsage> =
        transaction {
            ExposedGameRoundAiUsageTable
                .selectAll()
                .where { ExposedGameRoundAiUsageTable.roundId eq roundId.value }
                .orderBy(ExposedGameRoundAiUsageTable.seq, SortOrder.ASC)
                .map {
                    GameRoundAiUsage(
                        usage =
                            AiUsage(
                                purpose = it[ExposedGameRoundAiUsageTable.usagePurpose],
                                model = it[ExposedGameRoundAiUsageTable.modelName],
                                promptTokens = it[ExposedGameRoundAiUsageTable.promptTokens],
                                completionTokens = it[ExposedGameRoundAiUsageTable.completionTokens],
                                totalTokens = it[ExposedGameRoundAiUsageTable.totalTokens],
                            ),
                        playerId = it[ExposedGameRoundAiUsageTable.playerId]?.toGamePlayerId(),
                        recordedAt = it[ExposedGameRoundAiUsageTable.recordedAt],
                    )
                }
        }
}
