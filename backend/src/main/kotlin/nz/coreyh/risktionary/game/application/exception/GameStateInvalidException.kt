package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GameStateInvalidException : GameException(errorCode = ErrorCode.GAME_STATE_INVALID)
