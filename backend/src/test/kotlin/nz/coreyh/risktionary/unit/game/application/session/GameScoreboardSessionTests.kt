package nz.coreyh.risktionary.unit.game.application.session

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.game.application.session.GameScoreboardSession
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import org.junit.jupiter.api.Test

class GameScoreboardSessionTests {
    private val scoreboard = GameScoreboardSession()
    private val drawer = createTestGamePlayerId()
    private val alice = createTestGamePlayerId()
    private val bob = createTestGamePlayerId()

    @Test
    fun `a guesser earns the points of their correct guess`() {
        scoreboard.recordCorrectGuess(1, drawer, alice, 800)

        scoreboard.roundPoints(1)[alice] shouldBe 800
    }

    @Test
    fun `the drawer earns the average of the points of everyone who guessed correctly`() {
        scoreboard.recordCorrectGuess(1, drawer, alice, 800)
        scoreboard.recordCorrectGuess(1, drawer, bob, 400)

        scoreboard.drawerPoints(1) shouldBe 600
        scoreboard.roundPoints(1)[drawer] shouldBe 600
    }

    @Test
    fun `the drawer average is rounded`() {
        scoreboard.recordCorrectGuess(1, drawer, alice, 501)
        scoreboard.recordCorrectGuess(1, drawer, bob, 500)

        scoreboard.drawerPoints(1) shouldBe 501
    }

    @Test
    fun `the drawer earns nothing when nobody guessed correctly`() {
        scoreboard.drawerPoints(1) shouldBe 0
        scoreboard.roundPoints(1) shouldBe emptyMap()
    }

    @Test
    fun `a guesser earns points only once per round`() {
        scoreboard.recordCorrectGuess(1, drawer, alice, 800)
        scoreboard.recordCorrectGuess(1, drawer, alice, 200)

        scoreboard.roundPoints(1)[alice] shouldBe 800
    }

    @Test
    fun `totals add up points across rounds`() {
        scoreboard.recordCorrectGuess(1, drawer, alice, 800)
        scoreboard.recordCorrectGuess(2, alice, drawer, 600)
        scoreboard.recordCorrectGuess(2, alice, bob, 400)

        val totals = scoreboard.totalPoints()

        totals[alice] shouldBe 800 + 500
        totals[drawer] shouldBe 800 + 600
        totals[bob] shouldBe 400
    }

    @Test
    fun `rounds are scored separately`() {
        scoreboard.recordCorrectGuess(1, drawer, alice, 800)

        scoreboard.roundPoints(2) shouldBe emptyMap()
    }
}
