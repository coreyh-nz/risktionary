package nz.coreyh.risktionary.words.web.dto.response

import nz.coreyh.risktionary.words.domain.model.Word

data class CreateWordResponse(
    val word: Word,
)
