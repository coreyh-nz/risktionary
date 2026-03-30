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

    // User
    USER_NOT_FOUND(
        code = "auth.forbidden",
        httpStatus = HttpStatus.FORBIDDEN,
        defaultMessage = "You do not have permission to perform this action",
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
}
