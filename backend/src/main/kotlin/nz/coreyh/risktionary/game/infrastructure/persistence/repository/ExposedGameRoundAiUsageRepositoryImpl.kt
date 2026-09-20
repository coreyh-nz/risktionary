package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.game.domain.model.round.GameRoundAiUsage
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.repository.GameRoundAiUsageRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundAiUsageTable
import org.jetbrains.exposed.v1.jdbc.batchInsert
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
}
