package nz.coreyh.risktionary.shared.exception

/**
 * Thrown when a request cannot be authenticated.
 *
 * Typical causes:
 * - the access token is missing from the request,
 * - the token is malformed or has an invalid signature,
 * - the token is expired,
 * - the token fails validation for any other reason.
 */
class UnauthenticatedException : RuntimeException {
    constructor() : super()
    constructor(cause: Throwable) : super(cause)
}
