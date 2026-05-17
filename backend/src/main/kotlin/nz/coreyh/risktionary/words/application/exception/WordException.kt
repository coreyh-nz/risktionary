package nz.coreyh.risktionary.words.application.exception

import nz.coreyh.risktionary.shared.exception.AppException
import nz.coreyh.risktionary.shared.exception.code.ErrorCode

open class WordException(
    errorCode: ErrorCode,
) : AppException(errorCode)
