package nz.coreyh.risktionary.shared.exception.code

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    val defaultMessage: String,
) {
    // Auth
    AUTH_UNAUTHENTICATED(
        code = "auth.unauthenticated",
        httpStatus = HttpStatus.UNAUTHORIZED,
        defaultMessage = "Authentication is required",
    ),
    AUTH_FORBIDDEN(
        code = "auth.forbidden",
        httpStatus = HttpStatus.FORBIDDEN,
        defaultMessage = "You do not have permission to perform this action",
    ),

    // Game
    GAME_NOT_FOUND(
        code = "game.not-found",
        httpStatus = HttpStatus.NOT_FOUND,
        defaultMessage = "Game not found",
    ),
    GAME_TICKET_INVALID(
        code = "game.ticket-invalid",
        httpStatus = HttpStatus.BAD_REQUEST,
        defaultMessage = "Game ticket invalid",
    ),
    GAME_PLAYER_STATE_INVALID(
        code = "game.player-state-invalid",
        httpStatus = HttpStatus.BAD_REQUEST,
        defaultMessage = "Game player state invalid",
    ),
    GAME_PLAYER_ALREADY_IN_SESSION(
        code = "game.player-already-in-session",
        httpStatus = HttpStatus.BAD_REQUEST,
        defaultMessage = "Game player already in session",
    ),
    GAME_PLAYER_NOT_IN_SESSION(
        code = "game.player-not-in-session",
        httpStatus = HttpStatus.BAD_REQUEST,
        defaultMessage = "Game player not in session",
    ),
    GAME_PLAYER_DISPLAY_NAME_IN_USE(
        code = "game.player-display-name-in-use",
        httpStatus = HttpStatus.BAD_REQUEST,
        defaultMessage = "Game player not in use",
    ),

    // Generic
    INTERNAL_ERROR(
        code = "generic.internal-error",
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        defaultMessage = "An unexpected error occurred",
    ),
    INVALID_REQUEST(
        code = "generic.bad-request",
        httpStatus = HttpStatus.BAD_REQUEST,
        defaultMessage = "The request is invalid",
    ),
    NOT_FOUND(
        code = "generic.not-found",
        httpStatus = HttpStatus.NOT_FOUND,
        defaultMessage = "Not found",
    ),
}
