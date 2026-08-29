package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.words.domain.model.Word

class GameRoundSession(
    val id: RoundId,
    val game: GameSession,
    val word: Word,
) : LockableSession() {
    var state: GameRoundState = GameRoundState.SelectingDrawer
        get() = withLock { field }
        private set(value) = withLock { field = value }

    val guesses: GameRoundGuessSession = GameRoundGuessSession()
    val riskRatings: GameRoundRiskRatingSession = GameRoundRiskRatingSession()
    private val messages: MutableList<ChatMessage> = mutableListOf()

    /**
     * Transitions the round into the in-progress state with a confirmed drawer.
     *
     * Valid transitions:
     * - [RoundStateType.SELECTING_DRAWER] -> [RoundStateType.IN_PROGRESS]
     *
     * @param drawer the player selected to draw.
     * @throws GameRoundStateInvalidException if the round is not in the selecting drawer state.
     */
    fun selectDrawer(drawer: GamePlayerSession): Unit =
        withLock {
            requireState<GameRoundState.SelectingDrawer>()
            state = GameRoundState.InProgress(phase = GameRoundPhase.Initialising, drawer.player)
        }

    /**
     * Updates the phase of the current in-progress round.
     *
     * @throws GameRoundStateInvalidException if the round is not in progress.
     */
    fun updatePhase(phase: GameRoundPhase): Unit =
        withLock {
            val current = requireState<GameRoundState.InProgress>()
            state = current.copy(phase = phase)
        }

    fun complete() =
        withLock {
            requireState<GameRoundState.InProgress>()
            state = GameRoundState.Completed
        }

    fun addMessage(message: ChatMessage): Unit = withLock { messages.add(message) }
}

inline fun <reified T : GameRoundState> GameRoundSession.requireState(): T = state as? T ?: throw GameRoundStateInvalidException()
