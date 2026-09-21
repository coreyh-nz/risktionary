package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.GameRoundAiUsage
import kotlin.time.Clock

/**
 * Records the tokens spent on AI calls during a single round.
 */
class GameRoundAiUsageSession(
    private val clock: Clock = Clock.System,
) : LockableSession() {
    private val entries = mutableListOf<GameRoundAiUsage>()

    fun record(
        usage: AiUsage,
        playerId: GamePlayerId? = null,
    ) {
        withLock { entries.add(GameRoundAiUsage(usage, playerId, clock.now())) }
    }

    /** Every call recorded so far, in the order it was recorded. */
    fun getAll(): List<GameRoundAiUsage> = withLock { entries.toList() }
}
