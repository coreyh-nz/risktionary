package nz.coreyh.risktionary.game.socket.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.auth.domain.model.UserPrincipal
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GameTicketInvalidException
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.toGameIdOrNull
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketSessionAttributes
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

private val kLogger = KotlinLogging.logger {}

/**
 * Handshake interceptor that authenticates and authorizes WebSocket connections before STOMP session establishment.
 *
 * This interceptor validates two types of connection attempts:
 * - Player connections: Authenticated using a one-time game ticket provided as a query parameter
 * - Host connections: Authenticated using the existing HTTP session and Spring Security context
 *
 * On successful validation, a [GameSocketPrincipal] (either Player or Host) is stored in the handshake
 * attributes for later retrieval by [WebSocketHandshakeHandler]. The handshake then proceeds normally.
 *
 * On validation failure the handshake is still accepted, because browsers cannot read the status or body of a
 * failed WebSocket upgrade. Instead the [ErrorCode] describing the failure is stored in the handshake attributes
 * under [WebSocketSessionAttributes.CONNECT_ERROR], and [WebSocketChannelInterceptor] refuses the STOMP CONNECT
 * frame with an ERROR frame the client can read.
 */
@Component
class WebSocketHandshakeInterceptor(
    private val gameTicketService: GameTicketService,
    private val gameSessionStore: GameSessionStore,
) : HandshakeInterceptor {
    /**
     * Intercepts and validates the WebSocket handshake request before the WebSocket session is created.
     *
     * Determines the connection type based on query parameters:
     * - If a `ticket` parameter is present: attempts to authenticate as a game player
     * - If no `ticket` parameter: attempts to authenticate as a game host via the HTTP session
     *
     * @param request The HTTP request that initiated the WebSocket upgrade
     * @param response The HTTP response (unused; failures are reported via the handshake attributes)
     * @param wsHandler The WebSocket handler that will be used
     * @param attributes Mutable map for storing handshake attributes (populated on success)
     * @return always `true`; validation failures are recorded in [attributes] rather than rejecting the upgrade
     */
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val servletRequest =
            (request as? ServletServerHttpRequest)
                ?.servletRequest
                ?: run {
                    kLogger.debug { "Handshake rejected: request was not a servlet request" }
                    return attributes.refuse(ErrorCode.INVALID_REQUEST)
                }

        servletRequest.getParameter("ticket")?.let {
            return handlePlayerHandshake(it, attributes)
        }
        servletRequest.getParameter("gameId")?.let {
            return handleHostHandshake(it, attributes)
        }
        return attributes.refuse(ErrorCode.INVALID_REQUEST)
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
    }

    /**
     * Validates and authenticates a player WebSocket connection using a game ticket.
     *
     * Performs validation steps:
     * 1. Decodes the ticket - fails with [GameTicketInvalidException] if invalid or expired
     * 2. Verifies the game session exists - fails if game was never created or has ended
     * 3. Verifies the player has joined the game - fails if player is not a participant
     *
     * On success, creates a [GameSocketPrincipal.Player] and stores it in the handshake attributes
     * under the key "principal".
     *
     * @param ticketParam The raw ticket string from the query parameter
     * @param attributes The handshake attributes map to populate with the principal
     * @return always `true`; a failure is recorded via [refuse]
     */
    private fun handlePlayerHandshake(
        ticketParam: String,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val ticket =
            try {
                gameTicketService.decodeTicket(ticketParam)
            } catch (_: GameTicketInvalidException) {
                kLogger.debug { "Handshake rejected: ticket failed validation" }
                return attributes.refuse(ErrorCode.GAME_TICKET_INVALID)
            }

        val session = gameSessionStore.findById(ticket.gameId)
        if (session == null) {
            kLogger.debug { "Handshake rejected: ticket game's id is not valid" }
            return attributes.refuse(ErrorCode.GAME_NOT_FOUND)
        }

        try {
            session.getPlayer(ticket.playerId)
        } catch (_: GamePlayerNotInSessionException) {
            kLogger.debug { "Handshake rejected: ticket player's id has not joined the game" }
            return attributes.refuse(ErrorCode.GAME_PLAYER_NOT_IN_SESSION)
        }

        kLogger.debug {
            "Player handshake accepted: gameId=${ticket.gameId}, playerId=${ticket.playerId}"
        }

        val socketPrincipal =
            GameSocketPrincipal.Player(
                gameId = ticket.gameId,
                id = ticket.playerId,
            )
        attributes[WebSocketSessionAttributes.PRINCIPAL] = socketPrincipal
        return true
    }

    /**
     * Validates and authenticates a host WebSocket connection using the existing HTTP session.
     *
     * Performs validation steps:
     * 1. Extracts the authenticated [UserPrincipal] from the Spring Security context
     * 2. Parses the provided game ID - fails if the ID format is invalid
     * 3. Verifies the game session exists - fails if the game was never created or has ended
     * 4. Verifies the authenticated user is the host of the game session
     *
     * Host connections do not require a ticket because host privileges are established
     * through standard HTTP authentication.
     *
     * On success, creates a [GameSocketPrincipal.Host] and stores it in the handshake attributes
     * under the key `"principal"`.
     *
     * @param gameIdParam The raw game ID string from the query parameter
     * @param attributes The handshake attributes map to populate with the principal
     * @return always `true`; a failure is recorded via [refuse]
     */
    private fun handleHostHandshake(
        gameIdParam: String,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? UserPrincipal
        if (principal == null) {
            kLogger.debug { "Host handshake rejected: no authenticated principal" }
            return attributes.refuse(ErrorCode.AUTH_UNAUTHENTICATED)
        }

        val gameId = gameIdParam.toGameIdOrNull()
        if (gameId == null) {
            kLogger.debug { "Host handshake rejected: invalid gameId format: $gameIdParam" }
            return attributes.refuse(ErrorCode.GAME_NOT_FOUND)
        }

        val game = gameSessionStore.findById(gameId)
        if (game == null) {
            kLogger.debug { "Host handshake rejected: no game found for gameId=$gameId" }
            return attributes.refuse(ErrorCode.GAME_NOT_FOUND)
        }
        if (game.host.id != principal.userId) {
            kLogger.debug { "Host handshake rejected: user ${principal.userId} is not host of game ${game.id}" }
            return attributes.refuse(ErrorCode.GAME_NOT_FOUND)
        }

        val socketPrincipal =
            GameSocketPrincipal.Host(
                gameId = game.id,
                id = principal.userId,
            )
        attributes[WebSocketSessionAttributes.PRINCIPAL] = socketPrincipal

        kLogger.debug { "Host handshake accepted: gameId=${game.id}, userId=${principal.userId}" }
        return true
    }

    /**
     * Records why the connection must be refused so [WebSocketChannelInterceptor] can report it on STOMP CONNECT.
     *
     * A non-existent game and a game the user does not host both report [ErrorCode.GAME_NOT_FOUND] so that
     * game existence is not leaked to non-hosts.
     *
     * @return `true` so the handshake itself still proceeds
     */
    private fun MutableMap<String, Any>.refuse(errorCode: ErrorCode): Boolean {
        this[WebSocketSessionAttributes.CONNECT_ERROR] = errorCode
        return true
    }
}
