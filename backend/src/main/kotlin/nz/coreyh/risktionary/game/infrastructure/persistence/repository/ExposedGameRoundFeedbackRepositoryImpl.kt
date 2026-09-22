package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.feedback.domain.model.GeneratedFeedback
import nz.coreyh.risktionary.feedback.domain.model.toFeedbackId
import nz.coreyh.risktionary.game.domain.model.details.PersistedFeedback
import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.guess.toGuessId
import nz.coreyh.risktionary.game.domain.repository.GameRoundFeedbackRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundFeedbackGuessTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundFeedbackTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
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

    override fun findByRoundId(roundId: RoundId): List<PersistedFeedback> =
        transaction {
            val feedbackRows =
                ExposedGameRoundFeedbackTable
                    .selectAll()
                    .where { ExposedGameRoundFeedbackTable.roundId eq roundId.value }
                    .toList()
            if (feedbackRows.isEmpty()) return@transaction emptyList()

            val feedbackIds = feedbackRows.map { it[ExposedGameRoundFeedbackTable.id].value }
            val guessIdsByFeedbackId =
                ExposedGameRoundFeedbackGuessTable
                    .selectAll()
                    .where { ExposedGameRoundFeedbackGuessTable.feedbackId inList feedbackIds }
                    .groupBy({ it[ExposedGameRoundFeedbackGuessTable.feedbackId] }, { it[ExposedGameRoundFeedbackGuessTable.guessId] })

            feedbackRows.map {
                val id = it[ExposedGameRoundFeedbackTable.id].value
                PersistedFeedback(
                    id = id.toFeedbackId(),
                    playerId = it[ExposedGameRoundFeedbackTable.playerId].toGamePlayerId(),
                    framingCondition = it[ExposedGameRoundFeedbackTable.framingCondition],
                    timingCondition = it[ExposedGameRoundFeedbackTable.timingCondition],
                    status = it[ExposedGameRoundFeedbackTable.status],
                    factText = it[ExposedGameRoundFeedbackTable.factText],
                    framedText = it[ExposedGameRoundFeedbackTable.framedText],
                    generatedAt = it[ExposedGameRoundFeedbackTable.generatedAt],
                    sourceGuessIds = guessIdsByFeedbackId[id].orEmpty().map { guessId -> guessId.toGuessId() },
                )
            }
        }
}
