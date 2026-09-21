package nz.coreyh.risktionary.game.domain.model.round.chat

import nz.coreyh.risktionary.game.domain.model.round.guess.GuessId
import kotlin.time.Instant

/**
 * A [ChatMessage] as it was recorded in a round, with when it was sent
 * ([elapsedMs] is the time since the round drawing began, or null if
 * unknown) and, if it was produced by a guess, which guess.
 */
data class RecordedChatMessage(
    val message: ChatMessage,
    val sentAt: Instant,
    val elapsedMs: Long?,
    val guessId: GuessId?,
)
