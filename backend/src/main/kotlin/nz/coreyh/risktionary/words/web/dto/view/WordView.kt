package nz.coreyh.risktionary.words.web.dto.view

import nz.coreyh.risktionary.words.domain.model.Word
import nz.coreyh.risktionary.words.domain.model.WordId

data class WordView(
    val id: WordId,
    val value: String,
    val synonyms: List<String>,
    val descriptionText: String,
    val descriptionContent: String,
)

fun Word.toView(): WordView =
    WordView(
        id = id,
        value = value,
        synonyms = synonyms,
        descriptionText = descriptionText,
        descriptionContent = descriptionContent,
    )
