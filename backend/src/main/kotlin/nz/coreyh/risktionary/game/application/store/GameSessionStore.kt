package nz.coreyh.risktionary.game.application.store

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class GameSessionStore {
    private val sessions = ConcurrentHashMap<GameId, GameSession>()
    private val codeIndex: ConcurrentHashMap<String, GameId> = ConcurrentHashMap()

    fun getAll() = sessions.values.toList()

    fun findById(gameId: GameId): GameSession? = sessions[gameId]

    fun findByCode(code: String): GameSession? = codeIndex[code]?.let { sessions[it] }

    fun findByPlayerId(playerId: GamePlayerId): GameSession? = sessions.values.find { it.findPlayer(playerId) != null }

    fun addSession(
        gameId: GameId,
        gameSession: GameSession,
    ) {
        sessions[gameId] = gameSession
        codeIndex[gameSession.code] = gameId
    }

    fun remove(gameId: GameId) {
        sessions.remove(gameId)?.let { codeIndex.remove(it.code) }
    }

    fun clear() {
        sessions.clear()
    }
}
