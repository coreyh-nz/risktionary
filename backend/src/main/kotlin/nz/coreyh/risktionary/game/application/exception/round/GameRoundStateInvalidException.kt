package nz.coreyh.risktionary.game.application.exception.round

import nz.coreyh.risktionary.game.application.exception.GameException
import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GameRoundStateInvalidException : GameException(errorCode = ErrorCode.GAME_ROUND_STATE_INVALID)
