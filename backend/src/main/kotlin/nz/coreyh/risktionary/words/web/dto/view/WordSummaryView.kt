package nz.coreyh.risktionary.words.web.dto.view

import nz.coreyh.risktionary.words.domain.model.Word
import nz.coreyh.risktionary.words.domain.model.WordId

data class WordSummaryView(
    val id: WordId,
    val value: String,
    val synonyms: List<String>,
    val description: String,
)

fun Word.toSummaryView(): WordSummaryView =
    WordSummaryView(
        id = id,
        value = value,
        synonyms = synonyms,
        description = descriptionText,
    )
