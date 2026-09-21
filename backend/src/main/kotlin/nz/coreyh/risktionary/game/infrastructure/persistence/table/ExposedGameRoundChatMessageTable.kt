package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessageSystemType
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessageType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ExposedGameRoundChatMessageTable : UUIDTable("risktionary_game_round_chat_message") {
    val roundId = javaUUID("round_id").references(ExposedGameRoundTable.id, onDelete = ReferenceOption.CASCADE)
    val seq = integer("seq")
    val sentAt = timestamp("sent_at")
    val elapsedMs = long("elapsed_ms").nullable()
    val messageType = enumerationByName<ChatMessageType>("message_type", 16)
    val systemKind = enumerationByName<ChatMessageSystemType>("system_kind", 32).nullable()
    val playerId = javaUUID("player_id").references(ExposedGamePlayerTable.id).nullable()
    val chatText = text("chat_text").nullable()
    val guessId = javaUUID("guess_id").references(ExposedGameRoundGuessTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
}
