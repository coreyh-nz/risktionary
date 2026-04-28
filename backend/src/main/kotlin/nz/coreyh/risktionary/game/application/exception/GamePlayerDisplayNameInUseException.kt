package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GamePlayerDisplayNameInUseException : GameException(errorCode = ErrorCode.GAME_PLAYER_DISPLAY_NAME_IN_USE)
