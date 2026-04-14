package nz.coreyh.risktionary.game.application.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class GameTicketInvalidException : GameException(errorCode = ErrorCode.GAME_NOT_FOUND)
