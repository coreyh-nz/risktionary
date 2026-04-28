package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GamePlayerStateInvalidException : GameException(errorCode = ErrorCode.GAME_PLAYER_STATE_INVALID)
