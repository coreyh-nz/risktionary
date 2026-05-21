package nz.coreyh.risktionary.support.factory.word

import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.domain.model.Word
import nz.coreyh.risktionary.words.domain.model.WordId
import nz.coreyh.risktionary.words.domain.model.createWordId
import kotlin.time.Clock
import kotlin.time.Instant

fun createTestWord(
    id: WordId = createWordId(),
    value: String = "injury",
    synonyms: List<String> = listOf("damage", "harm"),
    descriptionText: String = "Injury",
    descriptionContent: String = "<p>Injury<p>",
    createdBy: UserId = createTestUserId(),
    createdAt: Instant = Clock.System.now(),
): Word =
    Word(
        id = id,
        value = value,
        synonyms = synonyms,
        descriptionText = descriptionText,
        descriptionContent = descriptionContent,
        createdAt = createdAt,
        createdBy = createdBy,
    )
