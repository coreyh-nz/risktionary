package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.words.domain.model.Word

class GameRoundSession(
    val id: RoundId,
    val gameId: GameId,
    val word: Word,
) : LockableSession() {
    var state: GameRoundState = GameRoundState.SelectingDrawer
        get() = withLock { field }
        set(value) = withLock { field = value }

    /**
     * Transitions the round into the in-progress state with a confirmed drawer.
     *
     * Valid transitions:
     * - [RoundStateType.SELECTING_DRAWER] -> [RoundStateType.IN_PROGRESS]
     *
     * @param drawerId the player selected to draw.
     * @throws GameRoundStateInvalidException if the round is not in the selecting drawer state.
     */
    fun selectDrawer(drawerId: GamePlayerId): Unit =
        withLock {
            requireState<GameRoundState.SelectingDrawer>()
            state = GameRoundState.InProgress(drawerId = drawerId)
        }

    /**
     * Asserts that the round is currently in the expected state and returns it.
     *
     * @throws GameRoundStateInvalidException if the current state does not match [T].
     */
    inline fun <reified T : GameRoundState> requireState(): T = state as? T ?: throw GameRoundStateInvalidException()
}
