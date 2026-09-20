package nz.coreyh.risktionary.unit.game.application.session.round

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.ai.infrastructure.dispatch.AiDispatcher
import nz.coreyh.risktionary.game.application.session.round.GameRoundFeedbackSession
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class GameRoundFeedbackSessionTests {
    private val session = GameRoundFeedbackSession()
    private val dispatcher = AiDispatcher()

    @AfterEach
    fun tearDown() {
        dispatcher.close()
    }

    @Test
    fun `awaiting with nothing pending returns immediately and settles the session`() {
        session.isSettled.shouldBeFalse()

        session.awaitPending(5.seconds) shouldBe 0

        session.isSettled.shouldBeTrue()
    }

    @Test
    fun `awaiting waits for a running generation to finish`() {
        val finished = AtomicBoolean(false)
        session.track(
            dispatcher.launch(
                block = { Thread.sleep(200) },
                onResult = { finished.set(true) },
            ),
        )

        session.awaitPending(5.seconds) shouldBe 0

        finished.get().shouldBeTrue()
    }

    @Test
    fun `awaiting abandons generations still running when the timeout passes`() {
        session.track(dispatcher.launch(block = { Thread.sleep(2_000) }, onResult = {}))
        session.track(dispatcher.launch(block = { }, onResult = {}))

        val abandoned = session.awaitPending(100.milliseconds)

        abandoned shouldBe 1
        session.isSettled.shouldBeTrue()
    }

    @Test
    fun `generations that already finished are not counted as abandoned`() {
        val job = dispatcher.launch(block = { }, onResult = {})
        session.track(job)
        Thread.sleep(200)

        session.awaitPending(100.milliseconds) shouldBe 0
    }
}
