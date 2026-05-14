package nz.coreyh.risktionary.words.domain.model

import nz.coreyh.risktionary.user.domain.model.UserId
import kotlin.time.Instant

data class Word(
    val id: WordId,
    val value: String,
    val synonyms: List<String>,
    val descriptionText: String,
    val descriptionContent: String,
    val createdBy: UserId,
    val createdAt: Instant,
)
