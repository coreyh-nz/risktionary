package nz.coreyh.risktionary.words.domain.repository

import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.domain.model.Word
import nz.coreyh.risktionary.words.domain.model.WordId
import kotlin.time.Instant

interface WordRepository {
    fun findAll(): List<Word>

    fun findById(id: WordId): Word?

    fun create(
        value: String,
        descriptionText: String,
        descriptionContent: String,
        synonyms: List<String>,
        createdBy: UserId,
        createdAt: Instant,
    ): Word

    fun update(
        id: WordId,
        value: String,
        descriptionText: String,
        descriptionContent: String,
        synonyms: List<String>,
    ): Word?

    fun delete(id: WordId): Boolean
}
