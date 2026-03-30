package nz.coreyh.risktionary.shared.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

/**
 * Thrown when a request cannot be authenticated.
 *
 * Typical causes:
 * - the access token is missing from the request,
 * - the token is malformed or has an invalid signature,
 * - the token is expired,
 * - the token is valid but the user does not exist.
 * - the token fails validation for any other reason.
 */
class UnauthenticatedException(
    cause: Throwable? = null,
) : AppException(
        errorCode = ErrorCode.AUTH_UNAUTHENTICATED,
        message = "Authentication is required",
        cause = cause,
    )
