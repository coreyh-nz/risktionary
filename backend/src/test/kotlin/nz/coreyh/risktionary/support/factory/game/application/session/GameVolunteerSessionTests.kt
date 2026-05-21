package nz.coreyh.risktionary.support.factory.game.application.session

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mockk.every
import io.mockk.mockk
import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.application.session.GameVolunteerSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@MockKTest
class GameVolunteerSessionTests {
    private lateinit var clock: Clock
    private lateinit var gameVolunteerSession: GameVolunteerSession

    private val now = Clock.System.now()
    private val t1 = now
    private val t2 = now + 1.seconds
    private val t3 = now + 2.seconds

    @BeforeEach
    fun setup() {
        clock = mockk(relaxed = true)
        gameVolunteerSession = GameVolunteerSession(clock)
    }

    @MockKTest
    @Nested
    inner class GetVolunteers {
        @Test
        fun `get volunteers returns players ordered by draw count ascending when draw counts differ`() {
            val player1 = createTestGamePlayerId()
            val player2 = createTestGamePlayerId()
            val player3 = createTestGamePlayerId()
            setupVolunteers(
                VolunteerSetup(id = player1, drawCount = 2),
                VolunteerSetup(id = player2, drawCount = 0),
                VolunteerSetup(id = player3, drawCount = 1),
            )

            val result = gameVolunteerSession.getVolunteers()

            result.shouldContainExactly(player2, player3, player1)
        }

        @Test
        fun `get volunteers returns players ordered by volunteer time ascending when draw counts are equal`() {
            val player1 = createTestGamePlayerId()
            val player2 = createTestGamePlayerId()
            val player3 = createTestGamePlayerId()
            setupVolunteers(
                VolunteerSetup(id = player1, at = t3),
                VolunteerSetup(id = player2, at = t1),
                VolunteerSetup(id = player3, at = t2),
            )

            val result = gameVolunteerSession.getVolunteers()

            result.shouldContainExactly(player2, player3, player1)
        }

        @Test
        fun `get volunteers returns players ordered by draw count then volunteer time when both differ`() {
            val player1 = createTestGamePlayerId()
            val player2 = createTestGamePlayerId()
            val player3 = createTestGamePlayerId()
            setupVolunteers(
                VolunteerSetup(id = player1, drawCount = 1, at = t1),
                VolunteerSetup(id = player2, drawCount = 1, at = t2),
                VolunteerSetup(id = player3, drawCount = 0, at = t3),
            )

            val result = gameVolunteerSession.getVolunteers()

            result.shouldContainExactly(player3, player1, player2)
        }
    }

    @MockKTest
    @Nested
    inner class IsVolunteering {
        @Test
        fun `is volunteering returns false when player has not volunteered`() {
            val player = createTestGamePlayerId()

            val result = gameVolunteerSession.isVolunteering(player)

            result.shouldBeFalse()
        }

        @Test
        fun `is volunteering returns true when player has volunteered`() {
            val player = createTestGamePlayerId()
            setupVolunteers(VolunteerSetup(id = player))

            val result = gameVolunteerSession.isVolunteering(player)

            result.shouldBeTrue()
        }
    }

    @MockKTest
    @Nested
    inner class Volunteer {
        @Test
        fun `volunteer adds player to volunteer pool when player has not volunteered`() {
            val player = createTestGamePlayerId()
            every { clock.now() } returns t1

            gameVolunteerSession.volunteer(player)

            gameVolunteerSession.getVolunteers().shouldContainExactly(player)
        }

        @Test
        fun `volunteer throws when player has already volunteered`() {
            val player = createTestGamePlayerId()
            setupVolunteers(VolunteerSetup(id = player))

            shouldThrow<GamePlayerStateInvalidException> {
                gameVolunteerSession.volunteer(player)
            }
        }

        @Test
        fun `volunteer records volunteer time using clock when player volunteers`() {
            val player1 = createTestGamePlayerId()
            val player2 = createTestGamePlayerId()
            setupVolunteers(
                VolunteerSetup(id = player1, at = t2),
                VolunteerSetup(id = player2, at = t1),
            )

            val result = gameVolunteerSession.getVolunteers()

            result.shouldContainExactly(player2, player1)
        }
    }

    @MockKTest
    @Nested
    inner class Unvolunteer {
        @Test
        fun `unvolunteer removes player from volunteer pool when player has volunteered`() {
            val player = createTestGamePlayerId()
            setupVolunteers(VolunteerSetup(id = player))

            gameVolunteerSession.unvolunteer(player)

            gameVolunteerSession.getVolunteers().shouldNotContain(player)
        }

        @Test
        fun `unvolunteer throws when player has not volunteered`() {
            val player = createTestGamePlayerId()

            shouldThrow<GamePlayerStateInvalidException> {
                gameVolunteerSession.unvolunteer(player)
            }
        }
    }

    @MockKTest
    @Nested
    inner class SelectDrawer {
        @Test
        fun `select drawer removes player from volunteer pool and increments draw count when player has volunteered`() {
            val player1 = createTestGamePlayerId()
            val player2 = createTestGamePlayerId()
            setupVolunteers(
                VolunteerSetup(id = player1),
                VolunteerSetup(id = player2),
            )

            gameVolunteerSession.selectDrawer(player1)
            gameVolunteerSession.volunteer(player1)

            gameVolunteerSession.getVolunteers().shouldContainExactly(player2, player1)
        }

        @Test
        fun `select drawer increments draw count from zero when player has not previously drawn`() {
            val player1 = createTestGamePlayerId()
            val player2 = createTestGamePlayerId()
            setupVolunteers(
                VolunteerSetup(id = player1),
                VolunteerSetup(id = player2),
            )

            gameVolunteerSession.selectDrawer(player1)
            gameVolunteerSession.volunteer(player1)

            gameVolunteerSession.getVolunteers().shouldContainExactly(player2, player1)
        }

        @Test
        fun `select drawer increments draw count from existing count when player has previously drawn`() {
            val player1 = createTestGamePlayerId()
            val player2 = createTestGamePlayerId()
            setupVolunteers(
                VolunteerSetup(id = player1, drawCount = 1),
                VolunteerSetup(id = player2, drawCount = 0),
            )

            gameVolunteerSession.selectDrawer(player1)
            gameVolunteerSession.volunteer(player1)

            gameVolunteerSession.getVolunteers().shouldContainExactly(player2, player1)
        }

        @Test
        fun `select drawer throws when player has not volunteered`() {
            val player = createTestGamePlayerId()

            shouldThrow<GamePlayerStateInvalidException> {
                gameVolunteerSession.selectDrawer(player)
            }
        }
    }

    @MockKTest
    @Nested
    inner class RemoveIfPresent {
        @Test
        fun `remove if present removes player from volunteer pool when player has volunteered`() {
            val player = createTestGamePlayerId()
            setupVolunteers(VolunteerSetup(id = player))

            gameVolunteerSession.removeIfPresent(player)

            gameVolunteerSession.getVolunteers().shouldNotContain(player)
        }

        @Test
        fun `remove if present does not throw when player has not volunteered`() {
            val player = createTestGamePlayerId()

            gameVolunteerSession.removeIfPresent(player)
        }
    }

    private data class VolunteerSetup(
        val id: GamePlayerId = createTestGamePlayerId(),
        val drawCount: Int = 0,
        val volunteered: Boolean = true,
        val at: Instant = Clock.System.now(),
    )

    private fun setupVolunteers(vararg entries: VolunteerSetup) {
        every { clock.now() } returnsMany entries.map { it.at }
        entries.forEach { (id, drawCount, volunteered, _) ->
            repeat(drawCount) {
                gameVolunteerSession.volunteer(id)
                gameVolunteerSession.selectDrawer(id)
            }
            if (volunteered) {
                gameVolunteerSession.volunteer(id)
            }
        }
    }
}
