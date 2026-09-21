package nz.coreyh.risktionary.game.infrastructure.persistence.repository

import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.game.domain.repository.GameRoundRepository
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundTable
import nz.coreyh.risktionary.words.domain.model.Word
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import kotlin.time.Instant

@Repository
class ExposedGameRoundRepositoryImpl : GameRoundRepository {
    override fun create(
        id: RoundId,
        gameId: GameId,
        roundNumber: Int,
        word: Word,
        drawerId: GamePlayerId,
        startedAt: Instant?,
        endedAt: Instant,
        finalState: RoundStateType,
        abandonedAiCalls: Int,
        drawerPoints: Int,
    ) {
        transaction {
            ExposedGameRoundTable.insert {
                it[ExposedGameRoundTable.id] = id.value
                it[ExposedGameRoundTable.gameId] = gameId.value
                it[ExposedGameRoundTable.roundNumber] = roundNumber
                it[ExposedGameRoundTable.wordId] = word.id.value
                it[ExposedGameRoundTable.wordValue] = word.value
                it[ExposedGameRoundTable.drawerId] = drawerId.value
                it[ExposedGameRoundTable.startedAt] = startedAt
                it[ExposedGameRoundTable.endedAt] = endedAt
                it[ExposedGameRoundTable.finalState] = finalState
                it[ExposedGameRoundTable.abandonedAiCalls] = abandonedAiCalls
                it[ExposedGameRoundTable.drawerPoints] = drawerPoints
            }
        }
    }

    override fun updateFinalState(
        id: RoundId,
        finalState: RoundStateType,
    ) {
        transaction {
            val count =
                ExposedGameRoundTable.update(where = { ExposedGameRoundTable.id eq id.value }) {
                    it[ExposedGameRoundTable.finalState] = finalState
                }
            check(count == 1) { "Expected to update exactly one round but updated $count (round=$id)" }
        }
    }
}
