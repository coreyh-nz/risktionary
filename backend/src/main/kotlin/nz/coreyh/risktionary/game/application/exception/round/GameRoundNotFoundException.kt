package nz.coreyh.risktionary.game.application.exception.round

import nz.coreyh.risktionary.game.application.exception.GameException
import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GameRoundNotFoundException : GameException(errorCode = ErrorCode.GAME_ROUND_NOT_FOUND)
