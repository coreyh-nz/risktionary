package nz.coreyh.risktionary.words.web.dto.response

import nz.coreyh.risktionary.words.web.dto.view.WordSummaryView

data class WordsResponse(
    val words: List<WordSummaryView>,
)
