package nz.coreyh.risktionary.game.application.service

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerDisplayNameInUseException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.application.service.round.GameRoundActionCoordinator
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionChatHandler
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.service.round.phase.GameRoundPhaseOrchestrator
import nz.coreyh.risktionary.game.application.service.round.phase.GameRoundPhaseTransitionService
import nz.coreyh.risktionary.game.application.service.round.phase.rating.GameRoundPhaseRiskRatingActionHandler
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.requireActiveRound
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
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
import nz.coreyh.risktionary.game.domain.model.risk.RiskRating
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
    private val gameRoundPhaseTransitionService: GameRoundPhaseTransitionService,
    private val gameRoundPhaseOrchestrator: GameRoundPhaseOrchestrator,
    private val gameRoundActionCoordinator: GameRoundActionCoordinator,
    private val gameRoundSessionChatHandler: GameRoundSessionChatHandler,
    private val gameRoundPhaseRiskRatingActionHandler: GameRoundPhaseRiskRatingActionHandler,
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
        session.transitionToStarting(timeWindow)
        gameEventPublisher.publishState(session)

        // schedule task to transition to in progress
        val block = { transitionToInProgress(gameId) }
        if (timeWindow.duration >= 0.seconds) {
            gameSessionTaskService.schedule(gameId, timeWindow.endingAt, block)
        } else {
            block()
        }
    }

    fun transitionToInProgress(gameId: GameId) {
        val session = getSession(gameId)
        session.transitionToInProgress()

        // TODO: replace this with actually getting a word
        val word = wordService.findWords().first()
        val round = gameRoundSessionService.createRound(game = session, word = word)
        session.currentRound = round

        gameEventPublisher.publishState(session)
    }

    /**
     * Registers a player as a volunteer to draw.
     *
     * @param gameId the game session.
     * @param playerId the player volunteering.
     * @throws GameNotFoundException if the session does not exist.
     * @throws GamePlayerNotInSessionException if the player is not active in
     *    the session.
     * @throws GamePlayerStateInvalidException if the player has already
     *    volunteered.
     */
    fun handleVolunteer(
        gameId: GameId,
        playerId: GamePlayerId,
    ) {
        val session = getSession(gameId)
        requireActivePlayer(session, playerId)
        session.volunteers.volunteer(playerId)

        gameEventPublisher.publishVolunteersUpdated(
            gameId = gameId,
            volunteers = session.volunteers.getVolunteers(),
        )
    }

    /**
     * Removes a player's volunteer nomination.
     *
     * @param gameId the game session.
     * @param playerId the player withdrawing their volunteer.
     * @throws GameNotFoundException if the session does not exist.
     * @throws GamePlayerNotInSessionException if the player is not active in
     *    the session.
     * @throws GamePlayerStateInvalidException if the player has not
     *    volunteered.
     */
    fun handleUnvolunteer(
        gameId: GameId,
        playerId: GamePlayerId,
    ) {
        val session = getSession(gameId)
        requireActivePlayer(session, playerId)
        session.volunteers.unvolunteer(playerId)

        gameEventPublisher.publishVolunteersUpdated(
            gameId = gameId,
            volunteers = session.volunteers.getVolunteers(),
        )
    }

    fun handleSelectDrawer(
        gameId: GameId,
        drawerId: GamePlayerId,
    ) {
        val session = getSession(gameId)
        val drawer = session.getPlayer(drawerId)
        val round = session.selectDrawer(drawer)

        // transition to the new first phase
        val phase = gameRoundPhaseTransitionService.next(round, (round.state as GameRoundState.InProgress).phase)
        phase ?: run { throw GameStateInvalidException() }
        round.updatePhase(phase)
        gameRoundPhaseOrchestrator.enterInitialPhase(round, phase)

        gameEventPublisher.publishRoundState(round)
        gameEventPublisher.publishVolunteersUpdated(gameId, session.volunteers.getVolunteers())
    }

    fun handleChat(
        gameId: GameId,
        playerId: GamePlayerId,
        text: String,
    ) {
        val session = getSession(gameId)
        val player = requireActivePlayer(session, playerId)
        gameRoundSessionChatHandler.handleChat(session, player, text)
    }

    fun handleRiskRating(
        gameId: GameId,
        playerId: GamePlayerId,
        rating: RiskRating,
    ) {
        val session = getSession(gameId)
        val round = session.requireActiveRound()
        val player = session.getPlayer(playerId)
        gameRoundActionCoordinator.submit(round, gameRoundPhaseRiskRatingActionHandler, player, rating)
    }

    private fun requireActivePlayer(
        session: GameSession,
        playerId: GamePlayerId,
    ) = session.getPlayer(playerId)

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
