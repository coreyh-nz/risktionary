package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.AppException
import nz.coreyh.risktionary.shared.exception.code.ErrorCode

open class GameException(
    errorCode: ErrorCode,
) : AppException(errorCode)
