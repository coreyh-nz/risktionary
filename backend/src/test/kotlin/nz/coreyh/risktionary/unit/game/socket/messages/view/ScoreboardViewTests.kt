package nz.coreyh.risktionary.unit.game.socket.messages.view

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.socket.messages.view.toScoreboardView
import nz.coreyh.risktionary.game.socket.messages.view.toStandingsView
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayer
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerIdentityGuest
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSession
import org.junit.jupiter.api.Test

class ScoreboardViewTests {
    private val game: GameSession = createTestGameSession()

    private fun join(
        name: String,
        status: GamePlayerStatus = GamePlayerStatus.ACTIVE,
    ): GamePlayerSession =
        createTestGamePlayerSession(
            createTestGamePlayer(identity = createTestGamePlayerIdentityGuest(name)),
            status,
        ).also { game.requestJoin(it) }

    @Test
    fun `players are ranked by total points with round points alongside`() {
        val drawer = join("Dee")
        val alice = join("Alice")
        val bob = join("Bob")
        game.scoreboard.recordCorrectGuess(1, drawer.id, alice.id, 800)
        game.scoreboard.recordCorrectGuess(1, drawer.id, bob.id, 400)

        val view = game.toScoreboardView(1)

        view.map { it.displayName } shouldBe listOf("Alice", "Dee", "Bob")
        view.map { it.totalPoints } shouldBe listOf(800, 600, 400)
        view.map { it.roundPoints } shouldBe listOf(800, 600, 400)
        view.map { it.rank } shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `round points and totals differ once there is more than one round`() {
        val drawer = join("Dee")
        val alice = join("Alice")
        game.scoreboard.recordCorrectGuess(1, drawer.id, alice.id, 800)
        game.scoreboard.recordCorrectGuess(2, alice.id, drawer.id, 500)

        val second = game.toScoreboardView(2).associateBy { it.displayName }

        second.getValue("Alice").roundPoints shouldBe 500
        second.getValue("Alice").totalPoints shouldBe 1300
        second.getValue("Dee").roundPoints shouldBe 500
        second.getValue("Dee").totalPoints shouldBe 1300
    }

    @Test
    fun `players with equal totals share a rank`() {
        val drawer = join("Dee")
        val alice = join("Alice")
        val bob = join("Bob")
        val cara = join("Cara")
        game.scoreboard.recordCorrectGuess(1, drawer.id, alice.id, 900)
        game.scoreboard.recordCorrectGuess(1, drawer.id, bob.id, 500)
        game.scoreboard.recordCorrectGuess(1, drawer.id, cara.id, 500)

        val view = game.toScoreboardView(1)

        view.map { it.displayName to it.rank } shouldBe listOf("Alice" to 1, "Dee" to 2, "Bob" to 3, "Cara" to 3)
    }

    @Test
    fun `players who scored nothing are still listed`() {
        join("Dee")
        join("Alice")

        val view = game.toScoreboardView(1)

        view.map { it.totalPoints } shouldBe listOf(0, 0)
        view.map { it.rank } shouldBe listOf(1, 1)
    }

    @Test
    fun `players who never connected are not listed`() {
        join("Dee")
        join("Ghost", GamePlayerStatus.PENDING)

        game.toScoreboardView(1).map { it.displayName } shouldBe listOf("Dee")
    }

    @Test
    fun `disconnected players stay on the scoreboard`() {
        val alice = join("Alice", GamePlayerStatus.DISCONNECTED)
        game.scoreboard.recordCorrectGuess(1, join("Dee").id, alice.id, 700)

        game.toScoreboardView(1).first().displayName shouldBe "Alice"
    }

    @Test
    fun `final standings rank by total points across every round`() {
        val drawer = join("Dee")
        val alice = join("Alice")
        game.scoreboard.recordCorrectGuess(1, drawer.id, alice.id, 800)

        val standings = game.toStandingsView()

        standings.map { it.displayName to it.totalPoints } shouldBe listOf("Alice" to 800, "Dee" to 800)
        standings.map { it.rank } shouldBe listOf(1, 1)
    }
}
