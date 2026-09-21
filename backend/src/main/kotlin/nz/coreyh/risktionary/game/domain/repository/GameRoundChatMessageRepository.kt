package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.chat.RecordedChatMessage

interface GameRoundChatMessageRepository {
    /** Inserts [messages], which must be in the order they were sent. */
    fun insertAll(
        roundId: RoundId,
        messages: List<RecordedChatMessage>,
    )
}
