package nz.coreyh.risktionary.words.application.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

class WordNotFoundException : WordException(errorCode = ErrorCode.WORD_NOT_FOUND)
