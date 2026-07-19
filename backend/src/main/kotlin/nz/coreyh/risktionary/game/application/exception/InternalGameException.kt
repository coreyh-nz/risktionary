package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.InternalAppException

open class InternalGameException(
    message: String? = null,
    cause: Throwable? = null,
) : InternalAppException(message = message, cause = cause)
