package nz.coreyh.risktionary.game.socket.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.auth.domain.model.UserPrincipal
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GameTicketInvalidException
import nz.coreyh.risktionary.game.application.service.GameTicketService
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.shared.web.dto.ApiErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import tools.jackson.databind.ObjectMapper

private val kLogger = KotlinLogging.logger {}

/**
 * Handshake interceptor that authenticates and authorizes WebSocket connections before STOMP session establishment.
 *
 * This interceptor validates two types of connection attempts:
 * - **Player connections**: Authenticated via a one-time ticket provided as a query parameter
 * - **Host connections**: Authenticated via the existing HTTP session's Spring Security context
 *
 * On successful validation, a [GameSocketPrincipal] (either Player or Host) is stored in the handshake
 * attributes for later retrieval by [WebSocketHandshakeHandler]. The handshake then proceeds normally.
 *
 * On validation failure, the handshake is rejected with HTTP 400 Bad Request and a JSON error response
 * containing [ErrorCode.GAME_TICKET_INVALID] with a descriptive message. The WebSocket connection is
 * closed before the STOMP protocol upgrades.
 */
@Component
class WebSocketHandshakeInterceptor(
    private val gameTicketService: GameTicketService,
    private val gameSessionStore: GameSessionStore,
    private val objectMapper: ObjectMapper,
) : HandshakeInterceptor {
    /**
     * Intercepts and validates the WebSocket handshake request before the WebSocket session is created.
     *
     * Determines the connection type based on query parameters:
     * - If a `ticket` parameter is present: attempts to authenticate as a game player
     * - If no `ticket` parameter: attempts to authenticate as a game host via the HTTP session
     *
     * @param request The HTTP request that initiated the WebSocket upgrade
     * @param response The HTTP response for rejecting the handshake if validation fails
     * @param wsHandler The WebSocket handler that will be used
     * @param attributes Mutable map for storing handshake attributes (populated on success)
     * @return `true` if validation passes and handshake should continue, `false` to reject the handshake
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
                    return response.rejectHandshake("Invalid request type")
                }

        val ticketParam = servletRequest.getParameter("ticket")
        return if (ticketParam != null) {
            handlePlayerHandshake(ticketParam, attributes, response)
        } else {
            handleHostHandshake(attributes, response)
        }
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
     * @param response The HTTP response to send rejection details if validation fails
     * @return `true` if validation passes, `false` if rejected
     */
    private fun handlePlayerHandshake(
        ticketParam: String,
        attributes: MutableMap<String, Any>,
        response: ServerHttpResponse,
    ): Boolean {
        val ticket =
            try {
                gameTicketService.decodeTicket(ticketParam)
            } catch (_: GameTicketInvalidException) {
                kLogger.debug { "Handshake rejected: ticket failed validation" }
                return response.rejectHandshake("Invalid ticket")
            }

        val session = gameSessionStore.findById(ticket.gameId)
        if (session == null) {
            kLogger.debug { "Handshake rejected: ticket game's id is not valid" }
            return response.rejectHandshake("Invalid ticket: game id is not valid")
        }

        try {
            session.getPlayer(ticket.playerId)
        } catch (_: GamePlayerNotInSessionException) {
            kLogger.debug { "Handshake rejected: ticket player's id has not joined the game" }
            return response.rejectHandshake("Invalid ticket: player id has not joined game")
        }

        kLogger.debug {
            "Player handshake accepted: gameId=${ticket.gameId}, playerId=${ticket.playerId}"
        }

        val socketPrincipal =
            GameSocketPrincipal.Player(
                gameId = ticket.gameId,
                id = ticket.playerId,
            )
        attributes["principal"] = socketPrincipal
        return true
    }

    /**
     * Validates and authenticates a host WebSocket connection using the existing HTTP session.
     *
     * Extracts the authenticated user from the Spring Security context and verifies they
     * have an active game session as the host. Host connections do not require a ticket
     * because host privileges are established through standard authentication.
     *
     * A host is considered valid if:
     * - There is an authenticated [UserPrincipal] in the security context
     * - That user has an active game session where they are the host
     *
     * On success, creates a [GameSocketPrincipal.Host] and stores it in the handshake attributes
     * under the key "principal".
     *
     * @param attributes The handshake attributes map to populate with the principal
     * @param response The HTTP response to send rejection details if validation fails
     * @return `true` if validation passes, `false` if rejected
     */
    private fun handleHostHandshake(
        attributes: MutableMap<String, Any>,
        response: ServerHttpResponse,
    ): Boolean {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? UserPrincipal
        if (principal == null) {
            kLogger.debug { "Host handshake rejected: no authenticated principal" }
            return response.rejectHandshake("Unauthorized")
        }

        val hostId = principal.userId
        val game = gameSessionStore.findByHostId(hostId)
        if (game == null) {
            kLogger.debug { "Host handshake rejected: no active game found for host with userId=${principal.name}" }
            return response.rejectHandshake("No active game found for host")
        }

        kLogger.debug {
            "Host handshake accepted: gameId=${game.id}, userId=${principal.name}"
        }

        val socketPrincipal =
            GameSocketPrincipal.Host(
                gameId = game.id,
                id = principal.userId,
            )
        attributes["principal"] = socketPrincipal
        return true
    }

    /**
     * Helper extension function that rejects a WebSocket handshake with an HTTP error response.
     *
     * Sets HTTP status to 400 Bad Request, Content-Type to application/json, and writes an
     * [ApiErrorResponse] containing the error code [ErrorCode.GAME_TICKET_INVALID] and the provided
     * message. This response is sent before the WebSocket protocol upgrade, allowing the client
     * to receive proper error semantics over HTTP.
     *
     * @param message The error message to include in the response body
     * @return `false` to indicate the handshake should be rejected
     */
    private fun ServerHttpResponse.rejectHandshake(message: String): Boolean {
        setStatusCode(HttpStatus.BAD_REQUEST)
        headers.contentType = MediaType.APPLICATION_JSON
        body.writer().apply {
            write(
                objectMapper.writeValueAsString(
                    ApiErrorResponse(
                        ErrorCode.GAME_TICKET_INVALID.code,
                        message,
                    ),
                ),
            )
            flush()
        }
        return false
    }
}
