package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GamePlayerAlreadyInSessionException : GameException(errorCode = ErrorCode.GAME_PLAYER_ALREADY_IN_SESSION)
