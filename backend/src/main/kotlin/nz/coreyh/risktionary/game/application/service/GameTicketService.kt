package nz.coreyh.risktionary.game.application.service

import nz.coreyh.risktionary.game.application.exception.GameTicketInvalidException
import nz.coreyh.risktionary.game.config.TicketProperties
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GameTicket
import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerIdOrNull
import nz.coreyh.risktionary.game.domain.model.toGameIdOrNull
import nz.coreyh.risktionary.shared.application.service.TokenService
import nz.coreyh.risktionary.shared.exception.UnauthenticatedException
import org.springframework.stereotype.Service
import kotlin.time.Clock

@Service
class GameTicketService(
    private val tokenService: TokenService,
    private val ticketProperties: TicketProperties,
    private val clock: Clock = Clock.System,
) {
    fun generateTicket(
        gameId: GameId,
        playerId: GamePlayerId,
    ): GameTicket {
        val issuedAt = clock.now()
        val expiresAt = issuedAt + ticketProperties.lifetime
        val token =
            tokenService.generateToken(
                subject = playerId.toString(),
                issuedAt = issuedAt,
                expiresAt = expiresAt,
                type = TOKEN_TYPE,
                claims = mapOf(GAME_CLAIM to gameId.toString()),
            )
        return GameTicket(
            gameId = gameId,
            playerId = playerId,
            issuedAt = token.issuedAt,
            expiresAt = token.expiresAt,
            value = token.value,
        )
    }

    fun decodeTicket(ticket: String): GameTicket {
        val token =
            try {
                tokenService.decodeToken(ticket, TOKEN_TYPE)
            } catch (e: Exception) {
                throw UnauthenticatedException(e)
            }

        val playerId =
            token.subject.toGamePlayerIdOrNull()
                ?: throw GameTicketInvalidException()
        val gameId =
            token.claims[GAME_CLAIM]
                ?.toGameIdOrNull()
                ?: throw GameTicketInvalidException()
        return GameTicket(
            gameId = gameId,
            playerId = playerId,
            issuedAt = token.issuedAt,
            expiresAt = token.expiresAt,
            value = token.value,
        )
    }

    companion object {
        const val TOKEN_TYPE = "game-ticket"
        const val GAME_CLAIM = "game"
    }
}
