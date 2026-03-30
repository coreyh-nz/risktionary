package nz.coreyh.risktionary.shared.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

abstract class AppException(
    val errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
