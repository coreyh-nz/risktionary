package nz.coreyh.risktionary.words.infrastructure.persistence.repository

import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.user.domain.model.toUserId
import nz.coreyh.risktionary.words.domain.model.Word
import nz.coreyh.risktionary.words.domain.model.WordId
import nz.coreyh.risktionary.words.domain.model.toWordId
import nz.coreyh.risktionary.words.domain.repository.WordRepository
import nz.coreyh.risktionary.words.infrastructure.persistence.table.ExposedWordSynonymTable
import nz.coreyh.risktionary.words.infrastructure.persistence.table.ExposedWordTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.Instant

@Repository
class ExposedWordRepositoryImpl : WordRepository {
    override fun findAll(): List<Word> =
        transaction {
            (ExposedWordTable leftJoin ExposedWordSynonymTable)
                .selectAll()
                .toWordList()
        }

    override fun findById(id: WordId): Word? =
        transaction {
            (ExposedWordTable leftJoin ExposedWordSynonymTable)
                .selectAll()
                .where { ExposedWordTable.id eq id.value }
                .toWord()
        }

    override fun create(
        value: String,
        descriptionText: String,
        descriptionContent: String,
        synonyms: List<String>,
        createdBy: UserId,
        createdAt: Instant,
    ): Word =
        transaction {
            val id =
                (
                    ExposedWordTable.insert {
                        it[ExposedWordTable.value] = value
                        it[ExposedWordTable.descriptionText] = descriptionText
                        it[ExposedWordTable.descriptionContent] = descriptionContent
                        it[ExposedWordTable.createdBy] = createdBy.value
                        it[ExposedWordTable.createdAt] = createdAt
                    } get ExposedWordTable.id
                ).value
            insertSynonyms(id, synonyms)

            Word(
                id = id.toWordId(),
                value = value,
                synonyms = synonyms,
                descriptionText = descriptionText,
                descriptionContent = descriptionContent,
                createdBy = createdBy,
                createdAt = createdAt,
            )
        }

    override fun update(
        id: WordId,
        value: String,
        descriptionText: String,
        descriptionContent: String,
        synonyms: List<String>,
    ): Word? =
        transaction {
            val count =
                ExposedWordTable.update(where = { ExposedWordTable.id eq id.value }) {
                    it[ExposedWordTable.value] = value
                    it[ExposedWordTable.descriptionText] = descriptionText
                    it[ExposedWordTable.descriptionContent] = descriptionContent
                }
            if (count == 0) return@transaction null

            ExposedWordSynonymTable.deleteWhere {
                ExposedWordSynonymTable.wordId eq id.value
            }
            insertSynonyms(id.value, synonyms)

            (ExposedWordTable leftJoin ExposedWordSynonymTable)
                .selectAll()
                .where { ExposedWordTable.id eq id.value }
                .toWord()
        }

    override fun delete(id: WordId): Boolean =
        transaction {
            val count =
                ExposedWordTable.deleteWhere {
                    ExposedWordTable.id eq id.value
                }
            count > 0
        }

    private fun insertSynonyms(
        id: UUID,
        synonyms: List<String>,
    ) {
        synonyms
            .takeIf { it.isNotEmpty() }
            ?.let {
                ExposedWordSynonymTable.batchInsert(it) { synonym ->
                    this[ExposedWordSynonymTable.wordId] = id
                    this[ExposedWordSynonymTable.value] = synonym
                }
            }
    }

    private fun Collection<ResultRow>.toWordAggregate(): Word {
        val first = first()
        return Word(
            id = first[ExposedWordTable.id].value.toWordId(),
            value = first[ExposedWordTable.value],
            synonyms = mapNotNull { it[ExposedWordSynonymTable.value] },
            descriptionText = first[ExposedWordTable.descriptionText],
            descriptionContent = first[ExposedWordTable.descriptionContent],
            createdBy = first[ExposedWordTable.createdBy].toUserId(),
            createdAt = first[ExposedWordTable.createdAt],
        )
    }

    private fun Query.toWordList(): List<Word> =
        groupBy { it[ExposedWordTable.id] }
            .values
            .map { it.toWordAggregate() }

    private fun Query.toWord(): Word? = toWordList().singleOrNull()
}
