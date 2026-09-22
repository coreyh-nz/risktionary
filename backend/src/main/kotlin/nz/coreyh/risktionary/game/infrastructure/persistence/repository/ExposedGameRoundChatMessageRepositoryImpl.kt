package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.game.domain.model.details.PersistedChatMessage
import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.chat.RecordedChatMessage
import nz.coreyh.risktionary.game.domain.model.round.chat.toChatMessageId
import nz.coreyh.risktionary.game.domain.model.round.guess.toGuessId
import nz.coreyh.risktionary.game.domain.repository.GameRoundChatMessageRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundChatMessageTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGameRoundChatMessageRepositoryImpl : GameRoundChatMessageRepository {
    override fun insertAll(
        roundId: RoundId,
        messages: List<RecordedChatMessage>,
    ) {
        if (messages.isEmpty()) return
        transaction {
            ExposedGameRoundChatMessageTable.batchInsert(messages.withIndex()) { (index, recorded) ->
                val message = recorded.message
                this[ExposedGameRoundChatMessageTable.id] = message.id.value
                this[ExposedGameRoundChatMessageTable.roundId] = roundId.value
                this[ExposedGameRoundChatMessageTable.seq] = index
                this[ExposedGameRoundChatMessageTable.sentAt] = recorded.sentAt
                this[ExposedGameRoundChatMessageTable.elapsedMs] = recorded.elapsedMs
                this[ExposedGameRoundChatMessageTable.messageType] = message.type
                this[ExposedGameRoundChatMessageTable.systemKind] = (message as? ChatMessage.System)?.kind
                this[ExposedGameRoundChatMessageTable.playerId] =
                    when (message) {
                        is ChatMessage.Player -> message.player.id.value
                        is ChatMessage.System.DrawerSelected -> message.player.id.value
                        is ChatMessage.System.CorrectGuess -> message.player.id.value
                        is ChatMessage.System.DrawingEndedAllGuessed -> null
                        is ChatMessage.System.DrawingEndedTimeUp -> null
                    }
                this[ExposedGameRoundChatMessageTable.chatText] = (message as? ChatMessage.Player)?.text
                this[ExposedGameRoundChatMessageTable.guessId] = recorded.guessId?.value
            }
        }
    }

    override fun findByRoundId(roundId: RoundId): List<PersistedChatMessage> =
        transaction {
            ExposedGameRoundChatMessageTable
                .selectAll()
                .where { ExposedGameRoundChatMessageTable.roundId eq roundId.value }
                .orderBy(ExposedGameRoundChatMessageTable.seq, SortOrder.ASC)
                .map {
                    PersistedChatMessage(
                        id = it[ExposedGameRoundChatMessageTable.id].value.toChatMessageId(),
                        sentAt = it[ExposedGameRoundChatMessageTable.sentAt],
                        elapsedMs = it[ExposedGameRoundChatMessageTable.elapsedMs],
                        type = it[ExposedGameRoundChatMessageTable.messageType],
                        systemKind = it[ExposedGameRoundChatMessageTable.systemKind],
                        playerId = it[ExposedGameRoundChatMessageTable.playerId]?.toGamePlayerId(),
                        text = it[ExposedGameRoundChatMessageTable.chatText],
                        guessId = it[ExposedGameRoundChatMessageTable.guessId]?.toGuessId(),
                    )
                }
        }
}
