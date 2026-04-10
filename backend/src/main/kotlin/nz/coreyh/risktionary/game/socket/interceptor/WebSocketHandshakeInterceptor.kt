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
 * This interceptor ensures that only authenticated and authorized players can establish a WebSocket
 * connection for a given game.
 */
@Component
class WebSocketHandshakeInterceptor(
    private val gameTicketService: GameTicketService,
    private val gameSessionStore: GameSessionStore,
    private val objectMapper: ObjectMapper,
) : HandshakeInterceptor {
    /**
     * Intercepts and validates incoming WebSocket handshake requests before a STOMP session is created.
     *
     * This interceptor enforces that every WebSocket connection supplies a valid `ticket` query parameter.
     * - The `ticket` query parameter must be present.
     * - The ticket must successfully decode via [GameTicketService].
     * - The referenced game must exist in [GameSessionStore].
     * - The referenced player must already be part of that game session.
     *
     * If validation fails, the handshake is rejected with:
     * - HTTP 400 Bad Request
     * - A JSON body containing an [ApiErrorResponse] with [ErrorCode.GAME_TICKET_INVALID]
     *
     * On success:
     * - The handshake proceeds.
     * - The decoded `gameId` and `playerId` are stored in the WebSocket session attributes.
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
