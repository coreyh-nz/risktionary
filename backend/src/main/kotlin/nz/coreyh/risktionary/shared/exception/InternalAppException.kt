package nz.coreyh.risktionary.shared.exception

abstract class InternalAppException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
