package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.words.domain.model.Word

class GameRoundSession(
    val id: RoundId,
    val word: Word,
) : LockableSession() {
    var state: GameRoundState = GameRoundState.SelectingDrawer
        get() = withLock { field }
        set(value) = withLock { field = value }

    /**
     * Transitions the round into the in-progress state with a confirmed drawer.
     *
     * Valid transitions:
     * - [GameRoundStateType.SELECTING_DRAWER] -> [GameRoundStateType.IN_PROGRESS]
     *
     * @param drawerId the player selected to draw.
     * @throws GameRoundStateInvalidException if the round is not in the selecting drawer state.
     * @throws GamePlayerStateInvalidException if the selected player did not volunteer.
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
    private inline fun <reified T : GameRoundState> requireState(): T = state as? T ?: throw GameRoundStateInvalidException()
}
