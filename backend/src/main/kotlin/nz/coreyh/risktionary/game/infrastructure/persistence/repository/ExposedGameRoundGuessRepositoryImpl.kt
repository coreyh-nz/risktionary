package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.guess.GameRoundGuess
import nz.coreyh.risktionary.game.domain.model.round.guess.toGuessId
import nz.coreyh.risktionary.game.domain.repository.GameRoundGuessRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundGuessTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedGameRoundGuessRepositoryImpl : GameRoundGuessRepository {
    override fun insertAll(
        roundId: RoundId,
        guesses: List<GameRoundGuess>,
    ) {
        if (guesses.isEmpty()) return
        transaction {
            ExposedGameRoundGuessTable.batchInsert(guesses.withIndex()) { (index, guess) ->
                this[ExposedGameRoundGuessTable.id] = guess.id.value
                this[ExposedGameRoundGuessTable.roundId] = roundId.value
                this[ExposedGameRoundGuessTable.playerId] = guess.playerId.value
                this[ExposedGameRoundGuessTable.seq] = index
                this[ExposedGameRoundGuessTable.guessText] = guess.text
                this[ExposedGameRoundGuessTable.guessResult] = guess.result
                this[ExposedGameRoundGuessTable.submittedAt] = guess.submittedAt
                this[ExposedGameRoundGuessTable.elapsedMs] = guess.elapsedMs
                this[ExposedGameRoundGuessTable.points] = guess.points
            }
        }
    }

    override fun findByRoundId(roundId: RoundId): List<GameRoundGuess> =
        transaction {
            ExposedGameRoundGuessTable
                .selectAll()
                .where { ExposedGameRoundGuessTable.roundId eq roundId.value }
                .orderBy(ExposedGameRoundGuessTable.seq, SortOrder.ASC)
                .map {
                    GameRoundGuess(
                        id = it[ExposedGameRoundGuessTable.id].value.toGuessId(),
                        playerId = it[ExposedGameRoundGuessTable.playerId].toGamePlayerId(),
                        text = it[ExposedGameRoundGuessTable.guessText],
                        result = it[ExposedGameRoundGuessTable.guessResult],
                        submittedAt = it[ExposedGameRoundGuessTable.submittedAt],
                        elapsedMs = it[ExposedGameRoundGuessTable.elapsedMs],
                        points = it[ExposedGameRoundGuessTable.points],
                    )
                }
        }
}
