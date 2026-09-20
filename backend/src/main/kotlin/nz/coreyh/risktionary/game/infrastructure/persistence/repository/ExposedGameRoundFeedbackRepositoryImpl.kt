package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.feedback.domain.model.GeneratedFeedback
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.repository.GameRoundFeedbackRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundFeedbackGuessTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundFeedbackTable
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGameRoundFeedbackRepositoryImpl : GameRoundFeedbackRepository {
    override fun insertAll(
        roundId: RoundId,
        feedback: List<GeneratedFeedback>,
    ) {
        if (feedback.isEmpty()) return
        transaction {
            ExposedGameRoundFeedbackTable.batchInsert(feedback) { generated ->
                this[ExposedGameRoundFeedbackTable.id] = generated.id.value
                this[ExposedGameRoundFeedbackTable.roundId] = roundId.value
                this[ExposedGameRoundFeedbackTable.playerId] = generated.playerId.value
                this[ExposedGameRoundFeedbackTable.framingCondition] = generated.framingCondition
                this[ExposedGameRoundFeedbackTable.timingCondition] = generated.timingCondition
                this[ExposedGameRoundFeedbackTable.status] = generated.status
                this[ExposedGameRoundFeedbackTable.factText] = generated.factText
                this[ExposedGameRoundFeedbackTable.framedText] = generated.framedText
                this[ExposedGameRoundFeedbackTable.generatedAt] = generated.generatedAt
            }

            val links = feedback.flatMap { generated -> generated.sourceGuessIds.map { generated.id.value to it.value } }
            if (links.isNotEmpty()) {
                ExposedGameRoundFeedbackGuessTable.batchInsert(links) { (feedbackId, guessId) ->
                    this[ExposedGameRoundFeedbackGuessTable.feedbackId] = feedbackId
                    this[ExposedGameRoundFeedbackGuessTable.guessId] = guessId
                }
            }
        }
    }
}
