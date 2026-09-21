package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.chat.RecordedChatMessage
import nz.coreyh.risktionary.game.domain.repository.GameRoundChatMessageRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundChatMessageTable
import org.jetbrains.exposed.v1.jdbc.batchInsert
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
}
