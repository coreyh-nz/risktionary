package nz.coreyh.risktionary.unit.game.domain.model.scoring

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.game.domain.model.scoring.GuessPointsCalculator
import nz.coreyh.risktionary.game.domain.model.scoring.ScoringConfiguration
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class GuessPointsCalculatorTests {
    private val config = ScoringConfiguration(maxPoints = 1000, minPoints = 100, untimedReferenceWindow = 60.seconds)

    private fun points(
        elapsedSeconds: Int?,
        durationSeconds: Int?,
    ) = GuessPointsCalculator.pointsFor(elapsedSeconds?.seconds, durationSeconds?.seconds, config)

    @Test
    fun `a guess at the same fraction of the round is worth the same whatever the round length`() {
        points(5, 10) shouldBe points(30, 60)
        points(5, 10) shouldBe 550
    }

    @Test
    fun `a guess at the very start earns the maximum`() {
        points(0, 60) shouldBe 1000
    }

    @Test
    fun `a guess as time runs out earns the minimum`() {
        points(60, 60) shouldBe 100
    }

    @Test
    fun `points fall linearly with the time used`() {
        points(15, 60) shouldBe 775
        points(30, 60) shouldBe 550
        points(45, 60) shouldBe 325
    }

    @Test
    fun `a guess made after the phase ended earns the minimum`() {
        points(90, 60) shouldBe 100
    }

    @Test
    fun `an untimed round is scored against the reference window`() {
        points(30, null) shouldBe points(30, 60)
        points(0, null) shouldBe 1000
    }

    @Test
    fun `an untimed guess after the reference window earns the minimum`() {
        points(600, null) shouldBe 100
    }

    @Test
    fun `unknown elapsed time is treated as the start of the round`() {
        points(null, 60) shouldBe 1000
        points(null, null) shouldBe 1000
    }

    @Test
    fun `a zero length phase is treated as untimed`() {
        points(30, 0) shouldBe points(30, null)
    }

    @Test
    fun `the configuration rejects a minimum above the maximum`() {
        shouldThrow<IllegalArgumentException> { ScoringConfiguration(100, 1000, 60.seconds) }
    }

    @Test
    fun `the configuration rejects a non-positive untimed window`() {
        shouldThrow<IllegalArgumentException> { ScoringConfiguration(1000, 100, 0.seconds) }
    }
}
