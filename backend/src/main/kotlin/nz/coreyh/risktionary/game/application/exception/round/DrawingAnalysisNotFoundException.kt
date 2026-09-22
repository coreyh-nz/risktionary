package nz.coreyh.risktionary.game.application.exception.round

import nz.coreyh.risktionary.game.application.exception.GameException
import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class DrawingAnalysisNotFoundException : GameException(errorCode = ErrorCode.GAME_DRAWING_ANALYSIS_NOT_FOUND)
