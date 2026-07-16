package nz.coreyh.risktionary.game.application.session

import nz.coreyh.risktionary.game.application.exception.GameNoMoreRoundsException
import nz.coreyh.risktionary.words.domain.model.Word

/**
 * Hands out words one at a time, in a fixed order, for successive rounds.
 */
class GameWordsSession(
    private val words: List<Word>,
) : LockableSession() {
    var index: Int = -1
        get() = withLock { field }
        private set

    fun hasNextWord(): Boolean = withLock { index + 1 < words.size }

    /**
     * Advances to the next word.
     *
     * @throws GameNoMoreRoundsException if all words have been used.
     */
    fun nextWord(): Word =
        withLock {
            if (!hasNextWord()) throw GameNoMoreRoundsException()
            index++
            words[index]
        }
}
