package nz.coreyh.risktionary.game.application.service

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.GamePlayerDisplayNameInUseException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.requireActiveRound
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.createGameId
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHost
import nz.coreyh.risktionary.game.domain.model.host.GameSessionHostStatus
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerIdentity
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import nz.coreyh.risktionary.game.domain.model.player.createPlayerId
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.hint.toWordHint
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.application.service.WordService
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
    private val wordService: WordService,
    private val gameRoundSessionService: GameRoundSessionService,
    private val gameSessionStore: GameSessionStore,
    private val gameEventPublisher: GameEventPublisher,
    private val clock: Clock = Clock.System,
) {
    /**
     * Creates a new in‑memory [GameSession] and registers it in the session store.
     *
     * @param hostId The user who created the game.
     *
     * @return The newly created session.
     */
    fun createSession(hostId: UserId): GameSession {
        val gameId = createGameId()
        val host = GameSessionHost(hostId, GameSessionHostStatus.Pending)
        val code = generateCode()
        val createdAt = clock.now()
        val session =
            GameSession(
                id = gameId,
                host = host,
                code = code,
                createdAt = createdAt,
            )
        gameSessionStore.addSession(gameId, session)
        return session
    }

    fun getSessions(): List<GameSession> = gameSessionStore.getAll()

    fun getSessionByCode(code: String): GameSession =
        gameSessionStore.findByCode(code)
            ?: throw GameNotFoundException()

    fun getSessionByHostId(hostId: UserId): GameSession =
        gameSessionStore.findByHostId(hostId)
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

        // TODO: replace this with actually getting a word
        val word = wordService.findWords().first()
        gameRoundSessionService.createRound(gameId = gameId, word = word)
    }

    /**
     * Registers a player as a volunteer to draw.
     *
     * @param gameId the game session.
     * @param playerId the player volunteering.
     * @throws GameNotFoundException if the session does not exist.
     * @throws GamePlayerNotInSessionException if the player is not active in the session.
     * @throws GamePlayerStateInvalidException if the player has already volunteered.
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
     * @throws GamePlayerNotInSessionException if the player is not active in the session.
     * @throws GamePlayerStateInvalidException if the player has not volunteered.
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
        val round = session.selectDrawer(drawerId)
        gameEventPublisher.publishRoundStateChanged(session.id, round.state)

        val wordHint = round.word.value.toWordHint()
        gameEventPublisher.publishVolunteersUpdated(gameId, session.volunteers.getVolunteers())
        gameEventPublisher.publishAssignedDrawerEvent(drawerId, round.word.value)
        gameEventPublisher.publishAssignedGuesserEvent(session.host.id, wordHint)
        session
            .getPlayers()
            .filter { it.id != drawerId }
            .forEach { gameEventPublisher.publishAssignedGuesserEvent(it.id, wordHint) }
    }

    fun handleChat(
        gameId: GameId,
        playerId: GamePlayerId,
        message: String,
    ) {
        val session = getSession(gameId)
        val round = session.currentRound.requireActiveRound()
        val player = requireActivePlayer(session, playerId)
        val word = round.word

        // todo move this into game round session then return a guess result or something
        val response =
            when {
                word.value.equals(message, ignoreCase = true) ||
                    word.synonyms.any {
                        it.equals(message, ignoreCase = true)
                    }
                -> {
                    ChatMessage.System.CorrectGuess(
                        playerId = playerId,
                        playerDisplayName = player.identity.displayName,
                    )
                }

                else -> {
                    ChatMessage.Player(
                        playerId = playerId,
                        playerDisplayName = player.identity.displayName,
                        text = message,
                    )
                }
            }
        round.addMessage(response)

        gameEventPublisher.publishRoundChatMessage(
            gameId = gameId,
            message = response,
        )
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
