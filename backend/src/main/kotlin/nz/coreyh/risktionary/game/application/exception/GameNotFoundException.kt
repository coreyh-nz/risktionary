package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GameNotFoundException : GameException(errorCode = ErrorCode.GAME_NOT_FOUND)
