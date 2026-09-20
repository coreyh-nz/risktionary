package nz.coreyh.risktionary.game.application.session.round

import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.LockableSession
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.words.domain.model.Word
import kotlin.time.Clock
import kotlin.time.Instant

class GameRoundSession(
    val id: RoundId,
    val game: GameSession,
    val word: Word,
    private val clock: Clock = Clock.System,
) : LockableSession() {
    var state: GameRoundState = GameRoundState.SelectingDrawer
        get() = withLock { field }
        private set(value) = withLock { field = value }

    /**
     * When the drawing phase began. This is the reference point for every
     * "time elapsed since the round started" statistic. Null until the round
     * enters [GameRoundPhase.Drawing].
     */
    var drawingStartedAt: Instant? = null
        get() = withLock { field }
        private set(value) = withLock { field = value }

    val drawing = GameRoundDrawingSession()
    val guesses = GameRoundGuessSession(clock, ::elapsedMsSinceDrawingStarted)
    val riskRatings = GameRoundRiskRatingSession()
    val feedback = GameRoundFeedbackSession()
    private val messages: MutableList<ChatMessage> = mutableListOf()

    /**
     * Milliseconds between the start of the drawing phase and [at], or null
     * if the drawing phase hasn't started.
     */
    fun elapsedMsSinceDrawingStarted(at: Instant): Long? = drawingStartedAt?.let { (at - it).inWholeMilliseconds }

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
            if (phase is GameRoundPhase.Drawing && drawingStartedAt == null) {
                drawingStartedAt = clock.now()
            }
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
