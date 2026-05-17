package nz.coreyh.risktionary.words.application.service

import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.application.exception.WordNotFoundException
import nz.coreyh.risktionary.words.domain.model.Word
import nz.coreyh.risktionary.words.domain.model.toWordIdOrNull
import nz.coreyh.risktionary.words.domain.repository.WordRepository
import org.springframework.stereotype.Service
import kotlin.time.Clock

@Service
class WordService(
    private val wordRepository: WordRepository,
    private val clock: Clock = Clock.System,
) {
    fun findWords(): List<Word> = wordRepository.findAll()

    fun getWord(id: String): Word =
        id
            .toWordIdOrNull()
            ?.let { wordRepository.findById(it) }
            ?: throw WordNotFoundException()

    fun createWord(
        value: String,
        descriptionText: String,
        descriptionContent: String,
        synonyms: List<String>,
        createdBy: UserId,
    ): Word =
        wordRepository.create(
            value = value,
            descriptionText = descriptionText,
            descriptionContent = descriptionContent,
            synonyms = synonyms,
            createdBy = createdBy,
            createdAt = clock.now(),
        )

    fun updateWord(
        id: String,
        value: String,
        descriptionText: String,
        descriptionContent: String,
        synonyms: List<String>,
    ): Word {
        val id = id.toWordIdOrNull() ?: throw WordNotFoundException()
        return wordRepository.update(
            id = id,
            value = value,
            descriptionText = descriptionText,
            descriptionContent = descriptionContent,
            synonyms = synonyms,
        ) ?: throw WordNotFoundException()
    }

    fun deleteWord(id: String) {
        val id = id.toWordIdOrNull() ?: throw WordNotFoundException()
        val deleted = wordRepository.delete(id)
        if (!deleted) {
            throw WordNotFoundException()
        }
    }
}
