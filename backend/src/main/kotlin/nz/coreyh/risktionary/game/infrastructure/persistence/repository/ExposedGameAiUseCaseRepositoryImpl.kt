package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.ai.domain.AiUseCaseSetup
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.repository.GameAiUseCaseRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameAiUseCaseTable
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGameAiUseCaseRepositoryImpl : GameAiUseCaseRepository {
    override fun insertAll(
        gameId: GameId,
        useCases: List<AiUseCaseSetup>,
    ) {
        if (useCases.isEmpty()) return
        transaction {
            ExposedGameAiUseCaseTable.batchInsert(useCases) { setup ->
                this[ExposedGameAiUseCaseTable.gameId] = gameId.value
                this[ExposedGameAiUseCaseTable.usagePurpose] = setup.purpose
                this[ExposedGameAiUseCaseTable.provider] = setup.provider
                this[ExposedGameAiUseCaseTable.modelName] = setup.model
                this[ExposedGameAiUseCaseTable.temperature] = setup.temperature
            }
        }
    }
}
