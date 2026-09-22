package nz.coreyh.risktionary.game.domain.model.details

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessageId
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessageSystemType
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessageType
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessId
import kotlin.time.Instant

data class PersistedChatMessage(
    val id: ChatMessageId,
    val sentAt: Instant,
    val elapsedMs: Long?,
    val type: ChatMessageType,
    val systemKind: ChatMessageSystemType?,
    val playerId: GamePlayerId?,
    val text: String?,
    /** The guess this message reported, if it was produced by one. */
    val guessId: GuessId?,
)
