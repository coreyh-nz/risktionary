package nz.coreyh.risktionary.game.socket.messages.view

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus

/**
 * One player line of the scoreboard shown at the end of a round.
 * Players with equal totals share a rank (1, 2, 2, 4).
 */
data class ScoreboardEntryView(
    val playerId: GamePlayerId,
    val displayName: String,
    val roundPoints: Int,
    val totalPoints: Int,
    val rank: Int,
)

/** One player line of the final standings shown when the game is over. */
data class StandingView(
    val playerId: GamePlayerId,
    val displayName: String,
    val totalPoints: Int,
    val rank: Int,
)

private data class PlayerScore(
    val playerId: GamePlayerId,
    val displayName: String,
    val roundPoints: Int,
    val totalPoints: Int,
)

/** Ranks by total points, highest first, with ties sharing a rank. Names break the display order of ties. */
private fun List<PlayerScore>.ranked(): List<Pair<PlayerScore, Int>> {
    val sorted = sortedWith(compareByDescending<PlayerScore> { it.totalPoints }.thenBy { it.displayName.lowercase() })
    return sorted.map { score ->
        val rank = sorted.indexOfFirst { it.totalPoints == score.totalPoints } + 1
        score to rank
    }
}

private fun GameSession.playerScores(roundNumber: Int): List<PlayerScore> {
    val round = scoreboard.roundPoints(roundNumber)
    val totals = scoreboard.totalPoints()
    return getPlayers()
        .filter { it.status != GamePlayerStatus.PENDING }
        .map {
            PlayerScore(
                playerId = it.id,
                displayName = it.player.identity.displayName,
                roundPoints = round[it.id] ?: 0,
                totalPoints = totals[it.id] ?: 0,
            )
        }
}

/** The scoreboard for [roundNumber]: points earned that round and running totals. */
fun GameSession.toScoreboardView(roundNumber: Int): List<ScoreboardEntryView> =
    playerScores(roundNumber).ranked().map { (score, rank) ->
        ScoreboardEntryView(score.playerId, score.displayName, score.roundPoints, score.totalPoints, rank)
    }

/** The final standings across every round. */
fun GameSession.toStandingsView(): List<StandingView> =
    playerScores(roundNumber).ranked().map { (score, rank) ->
        StandingView(score.playerId, score.displayName, score.totalPoints, rank)
    }
