package nz.coreyh.risktionary.game.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.game.application.command.CreateGameCommand
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerDisplayNameInUseException
import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameStateOrchestrator
import nz.coreyh.risktionary.game.application.service.round.GameRoundDrawingAnalysisService
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.requireActiveRound
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireInProgressPhase
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
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.domain.model.Word
import org.springframework.stereotype.Service
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

/**
 * Coordinates the lifecycle of active game sessions and the players within
 * them.
 */
@Service
class GameSessionService(
    private val gameSessionTaskService: GameSessionTaskService,
    private val gameTicketService: GameTicketService,
    private val gameRoundSessionService: GameRoundSessionService,
    private val gameRoundDrawingAnalysisService: GameRoundDrawingAnalysisService,
    private val gameSessionFeedbackAssignmentService: GameSessionFeedbackAssignmentService,
    private val gameStateOrchestrator: GameStateOrchestrator,
    private val gameSessionStore: GameSessionStore,
    private val gameEventPublisher: GameEventPublisher,
    private val clock: Clock = Clock.System,
) {
    /**
     * Creates a new in‑memory [GameSession] and registers it in the session
     * store.
     *
     * [command] is assumed to already be validated (see `CreateGameRequest.toCommand`).
     */
    fun createSession(command: CreateGameCommand): GameSession {
        val config =
            GameConfiguration(
                words = command.words,
                lobbyCountdown = command.lobbyCountdown,
                phaseDurations = command.phaseDurations,
                skippingCountdownsEnabled = command.skippingCountdownsEnabled,
                feedbackGenerationMode = FeedbackGenerationMode.AI,
            )
        val session =
            GameSession(
                id = createGameId(),
                host = GameSessionHost(command.hostId, GameSessionHostStatus.Pending),
                code = generateCode(),
                config = config,
                createdAt = clock.now(),
            )
        gameSessionStore.addSession(session.id, session)

        logger.debug {
            "Created game session: id=${session.id}, code=${session.code}, hostId=${session.host.id}, wordCount=${session.words.count()}"
        }

        return session
    }

    fun getSessions(): List<GameSession> = gameSessionStore.getAll()

    fun getSessionByCode(code: String): GameSession =
        gameSessionStore.findByCode(code)
            ?: throw GameNotFoundException()

    fun getSession(gameId: GameId): GameSession =
        gameSessionStore.findById(gameId)
            ?: throw GameNotFoundException()

    fun removeSession(gameId: GameId) {
        gameSessionStore.findById(gameId)?.let { session ->
            session.currentRound?.drawing?.close()
            if (session.config.feedbackGenerationEnabled) {
                session.feedback.feedbackDispatcher.close()
            }
        }
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

        // todo: need to come up with a better way cause this is specific to the study
        //  possibly feedback condition assignment using strategy pattern - idk
        if (gameSession.config.feedbackGenerationEnabled) {
            gameSessionFeedbackAssignmentService.assign(gameSession, player)
        }

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
        createNextRound(session)
        gameStateOrchestrator.transitionToStarting(session, timeWindow)
    }

    fun createNextRound(game: GameSession): GameRoundSession {
        val word = game.words.nextWord()
        val round = gameRoundSessionService.createRound(game = game, word = word)
        game.currentRound = round
        return round
    }

    fun getCurrentWord(gameId: GameId): Word {
        val session = getSession(gameId)
        val round = session.requireActiveRound()
        round.requireInProgressPhase<GameRoundPhase.WordReview>()
        return round.word
    }

    // TODO: idk if this should be here, its not an action
    fun handleDrawingSnapshot(
        gameId: GameId,
        dataUrl: String,
    ) {
        val session = getSession(gameId)
        if (session.config.feedbackGenerationMode != FeedbackGenerationMode.AI) return

        val round = session.requireActiveRound()

        // snapshots only matter while the drawing is in progress
        val state = round.state as? GameRoundState.InProgress
        if (state?.phase !is GameRoundPhase.Drawing) return

        gameRoundDrawingAnalysisService.analyse(
            round = round,
            aiDispatcher = round.drawing.aiDispatcher,
            dataUrl = dataUrl,
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
