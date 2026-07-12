package nz.coreyh.risktionary.game.application.session

import nz.coreyh.risktionary.game.application.exception.GamePlayerAlreadyInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.application.exception.round.GameRoundStateInvalidException
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.domain.model.GameStateType
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHost
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Represents the live, in‑memory state of an active game session.
 *
 * A `GameSession` is the authoritative runtime model for a game currently in progress.
 * It tracks connected players, manages their lifecycle transitions.
 *
 * - a player may only join once,
 * - only valid state transitions are allowed,
 * - operations must be performed on players that belong to this session.
 *
 * WebSocket events (CONNECT, DISCONNECT, STOMP frames, heartbeats) are handled by
 * multiple threads in the application. Because these events may mutate shared session
 * state concurrently, all access to the internal player map and session state is guarded
 * by a [ReentrantLock].
 */
class GameSession(
    val id: GameId,
    val host: GameSessionHost,
    val code: String,
    val createdAt: Instant,
    private val clock: Clock = Clock.System,
) : LockableSession() {
    private val players: MutableMap<GamePlayerId, GamePlayerSession> = mutableMapOf()
    val volunteers: GameVolunteerSession = GameVolunteerSession()

    /** The current lifecycle state of the game session. */
    var state: GameState = GameState.Lobby
        get() = withLock { field }
        set(value) = withLock { field = value }

    /** The timestamp of the most recent meaningful session activity.*/
    var lastActivityAt: Instant = createdAt
        get() = withLock { field }
        private set

    var currentRound: GameRoundSession? = null
        get() = withLock { field }
        set(value) = withLock { field = value }

    fun getPlayers(): List<GamePlayerSession> = withLock { players.values.toList() }

    fun getPlayer(playerId: GamePlayerId): GamePlayerSession = withLock { players[playerId] ?: throw GamePlayerNotInSessionException() }

    fun findPlayer(playerId: GamePlayerId): GamePlayerSession? = withLock { players[playerId] }

    /**
     * Registers a player as having requested to join the session.
     *
     * @param player the player session to register.
     * @throws GamePlayerAlreadyInSessionException if the player is already present.
     */
    fun requestJoin(player: GamePlayerSession): Unit =
        withLock {
            if (players.containsKey(player.id)) {
                throw GamePlayerAlreadyInSessionException()
            }
            withActivity { players[player.id] = player }
        }

    /**
     * Marks a player as attempting to establish a WebSocket connection.
     *
     * Valid transitions:
     * - [GamePlayerStatus.PENDING] -> [GamePlayerStatus.CONNECTING]
     * - [GamePlayerStatus.DISCONNECTED] -> [GamePlayerStatus.CONNECTING]
     *
     * @param playerId the identifier of the player connecting.
     * @throws GamePlayerNotInSessionException if the player does not belong to this session.
     * @throws GamePlayerStateInvalidException if the player's current state is invalid.
     */
    fun connect(playerId: GamePlayerId): Unit =
        withLock {
            val player = getPlayer(playerId)
            when (player.status) {
                GamePlayerStatus.PENDING,
                GamePlayerStatus.DISCONNECTED,
                -> {
                    withActivity { player.status = GamePlayerStatus.CONNECTING }
                }

                else -> {
                    throw GamePlayerStateInvalidException()
                }
            }
        }

    /**
     * Marks a player as fully connected and active in the session.
     *
     * Valid transitions:
     * - [GamePlayerStatus.CONNECTING] -> [GamePlayerStatus.ACTIVE]
     *
     * @param playerId the identifier of the player activating.
     * @return the activated player session.
     * @throws GamePlayerNotInSessionException if the player does not belong to this session.
     * @throws GamePlayerStateInvalidException if the player's current state is invalid.
     */
    fun activate(playerId: GamePlayerId): GamePlayerSession =
        withLock {
            val player = getPlayer(playerId)
            when (player.status) {
                GamePlayerStatus.CONNECTING -> {
                    withActivity { player.status = GamePlayerStatus.ACTIVE }
                }

                else -> {
                    throw GamePlayerStateInvalidException()
                }
            }
            player
        }

    /**
     * Marks a player as disconnected from the session.
     *
     * Valid transitions:
     * - [GamePlayerStatus.ACTIVE] -> [GamePlayerStatus.DISCONNECTED]
     *
     * @param playerId the identifier of the player disconnecting.
     * @throws GamePlayerNotInSessionException if the player does not belong to this session.
     * @throws GamePlayerStateInvalidException if the player's current state is invalid.
     */
    fun disconnect(playerId: GamePlayerId): Unit =
        withLock {
            val player = getPlayer(playerId)
            when (player.status) {
                GamePlayerStatus.ACTIVE -> {
                    withActivity { player.status = GamePlayerStatus.DISCONNECTED }
                }

                else -> {
                    throw GamePlayerStateInvalidException()
                }
            }
        }

    /**
     * Transitions the session into the starting state.
     *
     * Valid transitions:
     * - [GameStateType.LOBBY] -> [GameStateType.STARTING]
     *
     * @param startingAt the time the game will start.
     * @throws GameStateInvalidException if the current session state is invalid.
     */
    fun transitionToStarting(startingAt: Instant): Unit =
        withLock {
            requireState(GameStateType.LOBBY)
            withActivity { state = GameState.Starting(startingAt) }
        }

    /**
     * Transitions the session into the in-progress state.
     *
     * Valid transitions:
     * - [GameStateType.STARTING] -> [GameStateType.IN_PROGRESS]
     *
     * @throws GameStateInvalidException if the current session state is invalid.
     */
    fun transitionToInProgress(): Unit =
        withLock {
            requireState(GameStateType.STARTING)
            withActivity { state = GameState.InProgress }
        }

    /**
     * Selects the player who will act as the drawer for the current round.
     *
     * This operation delegates to both the volunteer manager and the active
     * [GameRoundSession], ensuring that:
     *
     * - the drawer is recorded at the session‑level volunteer tracker, and
     * - the drawer is registered within the current round.
     *
     * @param drawer the player being selected as drawer.
     * @return the updated [GameRoundSession].
     * @throws GameStateInvalidException if no round is currently active.
     * @throws GameRoundStateInvalidException if the round is not in the selecting drawer state.
     * @
     */
    fun selectDrawer(drawer: GamePlayerSession): GameRoundSession =
        withLock {
            val round = currentRound ?: throw GameStateInvalidException()
            round.requireState<GameRoundState.SelectingDrawer>()
            volunteers.selectDrawer(drawer.id)
            round.selectDrawer(drawer)
            round
        }

    /**
     * Verifies that the session is currently in the required state.
     *
     * @param requiredState the required current state type.
     * @throws GameStateInvalidException if the current state does not match.
     */
    private fun requireState(requiredState: GameStateType) {
        if (state.type != requiredState) throw GameStateInvalidException()
    }

    /**
     * Executes the given block and updates the session activity timestamp.
     *
     * This helper should be used for all mutating operations that represent
     * meaningful session activity.
     */
    private inline fun <T> withActivity(block: () -> T): T = block().also { lastActivityAt = clock.now() }
}

fun GameSession.requireActiveRound(): GameRoundSession = currentRound ?: throw GameRoundStateInvalidException()
