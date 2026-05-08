package nz.coreyh.risktionary.game.application.service

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerDisplayNameInUseException
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.createGameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerIdentity
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import nz.coreyh.risktionary.game.domain.model.player.createPlayerId
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.user.domain.model.UserId
import org.springframework.stereotype.Service
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Coordinates the lifecycle of active game sessions and the players within them.
 */
@Service
class GameSessionService(
    private val gameSessionTaskService: GameSessionTaskService,
    private val gameTicketService: GameTicketService,
    private val gameSessionStore: GameSessionStore,
    private val gameEventPublisher: GameEventPublisher,
    private val clock: Clock = Clock.System,
) {
    /**
     * Creates a new in‑memory [GameSession] and registers it in the session store.
     *
     * @param hostId The user who created the game.
     * @param code   The join code associated with the game.
     *
     * @return The newly created session.
     */
    fun createSession(hostId: UserId): GameSession {
        val gameId = createGameId()
        val code = generateCode()
        val session = GameSession(gameId, hostId, code)
        gameSessionStore.addSession(gameId, session)
        return session
    }

    fun getSessionByCode(code: String): GameSession =
        gameSessionStore.findByCode(code)
            ?: throw GameNotFoundException()

    fun getSession(gameId: GameId): GameSession =
        gameSessionStore.findById(gameId)
            ?: throw GameNotFoundException()

    fun removeSession(gameId: GameId) {
        gameSessionStore.remove(gameId)
        gameSessionTaskService.cancelAll(gameId)
    }

    /**
     * Handles the initial HTTP join request from a client.
     *
     * This represents the first step of the player lifecycle:
     * the player expresses intent to join the game, but has not yet
     * established a WebSocket connection.
     *
     * @return A ticket that authorizes the player to connect to the session.
     */
    fun handleJoinRequest(
        code: String,
        identity: GamePlayerIdentity,
    ): GameTicket {
        val session = getSessionByCode(code)

        // prevent duplicate display name
        session
            .getPlayers()
            .find { it.identity.displayName.equals(identity.displayName, ignoreCase = true) }
            ?.let {
                throw GamePlayerDisplayNameInUseException()
            }

        val playerSession =
            GamePlayerSession(
                createPlayerId(),
                identity,
            )

        session.requestJoin(playerSession)
        return gameTicketService.generateTicket(session.id, playerSession.id)
    }

    /**
     * Handles the HTTP request sent immediately before the WebSocket handshake.
     *
     * This represents the transition from "requested to join" to "attempting to connect".
     * The session validates that the ticket is valid and that the player is in a state
     * that allows connection.
     */
    fun handleConnecting(
        gameId: GameId,
        playerId: GamePlayerId,
    ) {
        val gameSession = getSession(gameId)
        gameSession.connect(playerId)
    }

    /**
     * Handles the STOMP CONNECTED frame, indicating that the WebSocket connection
     * has been fully established.
     *
     * This represents the transition into the ACTIVE state, meaning the player is now
     * a live participant in the session.
     */
    fun handleConnected(
        gameId: GameId,
        playerId: GamePlayerId,
    ) {
        val gameSession = getSession(gameId)
        val player = gameSession.activate(playerId)

        gameEventPublisher.publishPlayerJoined(
            gameId = gameId,
            player = player,
        )
    }

    fun handleReady(playerId: GamePlayerId) {
        val session =
            gameSessionStore.findByPlayerId(playerId)
                ?: throw GameNotFoundException()
        gameEventPublisher.publishPlayerList(playerId, session.getPlayers())
    }

    fun handleDisconnected(
        gameId: GameId,
        playerId: GamePlayerId,
    ) {
        val gameSession = getSession(gameId)
        gameSession.disconnect(playerId)

        gameEventPublisher.publishPlayerLeft(
            gameId = gameId,
            playerId = playerId,
        )
    }

    fun transitionToStarting(gameId: GameId) {
        val session = getSession(gameId)

        // todo - change this to use time from settings when implemented
        val startAt = 10.seconds
        session.transitionToStarting(startAt)

        gameEventPublisher.publishStateChanged(
            gameId = gameId,
            gameState = session.state,
        )

        // schedule task to transition to in progress
        gameSessionTaskService.schedule(gameId, clock.now() + startAt) { transitionToInProgress(gameId) }
    }

    fun transitionToInProgress(gameId: GameId) {
        val session = getSession(gameId)
        session.transitionToInProgress()

        gameEventPublisher.publishStateChanged(
            gameId = gameId,
            gameState = session.state,
        )
    }

    private fun generateCode(): String {
        repeat(10) {
            val code =
                (1..CODE_LENGTH)
                    .map { CODE_CHARACTERS.random() }
                    .joinToString("")
            if (gameSessionStore.findByCode(code) == null) {
                return code
            }
        }
        throw IllegalStateException("Could not create game unique code")
    }

    companion object {
        const val CODE_LENGTH = 6
        const val CODE_CHARACTERS = "0123456789"
    }
}
