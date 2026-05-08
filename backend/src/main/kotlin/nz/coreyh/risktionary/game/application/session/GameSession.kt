package nz.coreyh.risktionary.game.application.session

import nz.coreyh.risktionary.game.application.exception.GamePlayerAlreadyInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.domain.model.GameStateType
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.user.domain.model.UserId
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

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
    val hostId: UserId,
    val code: String,
) {
    private val players: MutableMap<GamePlayerId, GamePlayerSession> = mutableMapOf()
    private val lock = ReentrantLock()

    var state: GameState = GameState.Lobby
        get() = lock.withLock { field }
        set(value) = lock.withLock { field = value }

    fun getPlayers(): List<GamePlayerSession> = lock.withLock { players.values.toList() }

    fun getPlayer(playerId: GamePlayerId): GamePlayerSession =
        lock.withLock { players[playerId] ?: throw GamePlayerNotInSessionException() }

    fun findPlayer(playerId: GamePlayerId): GamePlayerSession? = lock.withLock { players[playerId] }

    /**
     * Registers a player as having requested to join the session.
     *
     * @throws GamePlayerAlreadyInSessionException if the player is already present.
     */
    fun requestJoin(player: GamePlayerSession) =
        lock.withLock {
            if (players.containsKey(player.id)) {
                throw GamePlayerAlreadyInSessionException()
            }
            players[player.id] = player
        }

    /**
     * Marks a player as attempting to establish a WebSocket connection.
     */
    fun connect(playerId: GamePlayerId) =
        lock.withLock {
            val player = getPlayer(playerId)
            when (player.status) {
                GamePlayerStatus.PENDING,
                GamePlayerStatus.DISCONNECTED,
                -> player.status = GamePlayerStatus.CONNECTING

                else -> throw GamePlayerStateInvalidException()
            }
        }

    /**
     * Marks a player as fully connected and active in the session.
     */
    fun activate(playerId: GamePlayerId): GamePlayerSession =
        lock.withLock {
            val player = getPlayer(playerId)
            when (player.status) {
                GamePlayerStatus.CONNECTING -> {
                    player.status = GamePlayerStatus.ACTIVE
                }

                else -> {
                    throw GamePlayerStateInvalidException()
                }
            }
            player
        }

    fun disconnect(playerId: GamePlayerId) =
        lock.withLock {
            val player = getPlayer(playerId)
            when (player.status) {
                GamePlayerStatus.ACTIVE -> {
                    player.status = GamePlayerStatus.DISCONNECTED
                }

                else -> {
                    throw GamePlayerStateInvalidException()
                }
            }
        }

    fun transitionToStarting(startingIn: Duration) {
        lock.withLock {
            requireState(GameStateType.LOBBY)
            state = GameState.Starting(startingIn)
        }
    }

    fun transitionToInProgress() {
        lock.withLock {
            requireState(GameStateType.STARTING)
            state = GameState.InProgress
        }
    }

    private fun requireState(requiredState: GameStateType) {
        if (state.type != requiredState) throw GameStateInvalidException()
    }
}
