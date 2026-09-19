package nz.coreyh.risktionary.game.web.dto

import nz.coreyh.risktionary.words.domain.model.Word

data class WordReviewView(
    val value: String,
    val synonyms: List<String>,
    val descriptionText: String,
    val descriptionContent: String,
)

fun Word.toReviewView(): WordReviewView =
    WordReviewView(
        value = value,
        synonyms = synonyms,
        descriptionText = descriptionText,
        descriptionContent = descriptionContent,
    )
