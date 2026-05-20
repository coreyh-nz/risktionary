package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.words.domain.model.Word

class GameRoundSession(
    val id: RoundId,
    val word: Word,
) : LockableSession() {
    var state: GameRoundState = GameRoundState.SelectingDrawer
        get() = withLock { field }
        set(value) = withLock { field = value }
}
