package nz.coreyh.risktionary.game.application.service

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerDisplayNameInUseException
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameRoundPhaseStateOrchestrator
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameStateOrchestrator
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhaseStateTransitionService
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.TimeWindow
import nz.coreyh.risktionary.game.domain.model.createGameId
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHost
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHostStatus
import nz.coreyh.risktionary.game.domain.model.player.GamePlayer
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerIdentity
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import nz.coreyh.risktionary.game.domain.model.player.createPlayerId
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.application.service.WordService
import org.springframework.stereotype.Service
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Coordinates the lifecycle of active game sessions and the players within
 * them.
 */
@Service
class GameSessionService(
    private val gameSessionTaskService: GameSessionTaskService,
    private val gameTicketService: GameTicketService,
    private val wordService: WordService,
    private val gameRoundSessionService: GameRoundSessionService,
    private val gameRoundPhaseStateTransitionService: GameRoundPhaseStateTransitionService,
    private val gameRoundPhaseStateOrchestrator: GameRoundPhaseStateOrchestrator,
    private val gameStateOrchestrator: GameStateOrchestrator,
    private val gameSessionStore: GameSessionStore,
    private val gameEventPublisher: GameEventPublisher,
    private val clock: Clock = Clock.System,
) {
    /**
     * Creates a new in‑memory [GameSession] and registers it in the session
     * store.
     *
     * @param hostId The user who created the game.
     * @return The newly created session.
     */
    fun createSession(hostId: UserId): GameSession {
        // todo - make this configurable
        val config =
            GameConfiguration(
                words = wordService.findWords(),
                lobbyCountdown = 5.seconds,
                phaseDurations =
                    mapOf(
                        RoundPhaseType.DRAWING_REVIEW to 30.seconds,
                    ),
                skippingCountdownsEnabled = true,
            )
        return GameSession(
            id = createGameId(),
            host = GameSessionHost(hostId, GameSessionHostStatus.Pending),
            code = generateCode(),
            config = config,
            createdAt = clock.now(),
        ).also {
            gameSessionStore.addSession(it.id, it)
        }
    }

    fun getSessions(): List<GameSession> = gameSessionStore.getAll()

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
     * This represents the first step of the player lifecycle: the player
     * expresses intent to join the game, but has not yet established a
     * WebSocket connection.
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
            .find {
                it.player.identity.displayName
                    .equals(identity.displayName, ignoreCase = true)
            }?.let {
                throw GamePlayerDisplayNameInUseException()
            }

        val player = GamePlayer(createPlayerId(), identity)
        val playerSession = GamePlayerSession(player, GamePlayerStatus.PENDING)

        session.requestJoin(playerSession)
        return gameTicketService.generateTicket(session.id, playerSession.id)
    }

    /**
     * Handles the HTTP request sent immediately before the WebSocket
     * handshake.
     *
     * This represents the transition from "requested to join" to "attempting
     * to connect". The session validates that the ticket is valid and that the
     * player is in a state that allows connection.
     */
    fun handleConnecting(
        gameId: GameId,
        playerId: GamePlayerId,
    ) {
        val gameSession = getSession(gameId)
        gameSession.connect(playerId)
    }

    /**
     * Handles the STOMP CONNECTED frame, indicating that the WebSocket
     * connection has been fully established.
     *
     * This represents the transition into the ACTIVE state, meaning the player
     * is now a live participant in the session.
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

    /**
     * Handles the point when a player has subscribed to all necessary
     * topics/queues and is ready to receive the full current game state.
     *
     * Sends a complete initial synchronisation to the player.
     */
    fun handleReady(playerId: GamePlayerId) {
        val game =
            gameSessionStore.findByPlayerId(playerId)
                ?: throw GameNotFoundException()

        gameEventPublisher.publishStateToPlayer(playerId, game)
        gameEventPublisher.publishPlayerList(playerId = playerId, players = game.getPlayers())
        gameEventPublisher.publishVolunteersUpdated(gameId = game.id, volunteers = game.volunteers.getVolunteers())
    }

    /**
     * Handles the point when a host has subscribed to all necessary
     * topics/queues and is ready to receive the full current game state.
     *
     * Sends a complete initial synchronisation to the player.
     */
    fun handleHostReady(userId: UserId) {
        val game =
            gameSessionStore.findByHostId(userId)
                ?: throw GameNotFoundException()

        gameEventPublisher.publishStateToHost(userId, game)
        gameEventPublisher.publishPlayerListToHost(userId = userId, players = game.getPlayers())
        gameEventPublisher.publishVolunteersUpdatedToHost(userId = userId, volunteers = game.volunteers.getVolunteers())
    }

    fun handleDisconnected(
        gameId: GameId,
        playerId: GamePlayerId,
    ) {
        val session = getSession(gameId)
        session.disconnect(playerId)
        session.volunteers.removeIfPresent(playerId)

        gameEventPublisher.publishPlayerLeft(
            gameId = gameId,
            playerId = playerId,
        )
        gameEventPublisher.publishVolunteersUpdated(
            gameId = gameId,
            volunteers = session.volunteers.getVolunteers(),
        )
    }

    fun handleHostConnected(
        hostId: UserId,
        gameId: GameId,
    ) {
        val session = getSession(gameId)
        if (session.host.id != hostId) throw GameNotFoundException()

        session.host.connect(clock.now())
        gameEventPublisher.publishStateToHost(hostId, session)
    }

    fun handleHostDisconnected(
        hostId: UserId,
        gameId: GameId,
    ) {
        val session = getSession(gameId)
        if (session.host.id != hostId) throw GameNotFoundException()

        session.host.disconnect(clock.now())
    }

    fun transitionToStarting(gameId: GameId) {
        val session = getSession(gameId)
        val timeWindow = TimeWindow(startedAt = clock.now(), duration = session.config.lobbyCountdown)
        createRound(session)
        gameStateOrchestrator.transitionToStarting(session, timeWindow)
    }

    fun createRound(game: GameSession) {
        val word = game.words.nextWord()
        val round = gameRoundSessionService.createRound(game = game, word = word)
        game.currentRound = round
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
