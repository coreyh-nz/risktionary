package nz.coreyh.risktionary.unit.auth.application.session

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.game.application.exception.GamePlayerAlreadyInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.application.exception.GameStateInvalidException
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class GameSessionTests {
    private lateinit var session: GameSession

    @BeforeEach
    fun setup() {
        session = createTestGameSession()
    }

    @Nested
    inner class GetPlayer {
        @Test
        fun `get player returns player when player exists`() {
            val player = createTestGamePlayerSession()
            session.requestJoin(player)

            val result = session.getPlayer(player.id)

            result shouldBe player
        }

        @Test
        fun `get player throws when player does not exist`() {
            shouldThrow<GamePlayerNotInSessionException> {
                session.getPlayer(createTestGamePlayerId())
            }
        }
    }

    @Nested
    inner class FindPlayer {
        @Test
        fun `find player returns player when player exists`() {
            val player = createTestGamePlayerSession()
            session.requestJoin(player)

            val result = session.findPlayer(player.id)

            result shouldBe player
        }

        @Test
        fun `find player returns null when player does not exist`() {
            val result = session.findPlayer(createTestGamePlayerId())

            result shouldBe null
        }
    }

    @Nested
    inner class RequestJoin {
        @Test
        fun `request join adds player when player is not already in session`() {
            val player = createTestGamePlayerSession()

            session.requestJoin(player)

            session.getPlayer(player.id) shouldBe player
        }

        @Test
        fun `request join throws when player is already in session`() {
            val player = createTestGamePlayerSession()

            session.requestJoin(player)

            shouldThrow<GamePlayerAlreadyInSessionException> {
                session.requestJoin(player)
            }
        }
    }

    @Nested
    inner class Connect {
        @Test
        fun `connect changes player status from pending to connecting`() {
            val player = createTestGamePlayerSession(status = GamePlayerStatus.PENDING)
            session.requestJoin(player)

            session.connect(player.id)

            player.status shouldBe GamePlayerStatus.CONNECTING
        }

        @Test
        fun `connect changes player status from disconnected to connecting`() {
            val player = createTestGamePlayerSession(status = GamePlayerStatus.DISCONNECTED)
            session.requestJoin(player)

            session.connect(player.id)

            player.status shouldBe GamePlayerStatus.CONNECTING
        }

        @Test
        fun `connect throws when player status is invalid`() {
            val player = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
            session.requestJoin(player)

            shouldThrow<GamePlayerStateInvalidException> {
                session.connect(player.id)
            }
        }

        @Test
        fun `connect throws when player does not exist`() {
            shouldThrow<GamePlayerNotInSessionException> {
                session.connect(createTestGamePlayerId())
            }
        }
    }

    @Nested
    inner class Activate {
        @Test
        fun `activate changes player status from connecting to active`() {
            val player = createTestGamePlayerSession(status = GamePlayerStatus.CONNECTING)
            session.requestJoin(player)

            val result = session.activate(player.id)

            result shouldBe player
            player.status shouldBe GamePlayerStatus.ACTIVE
        }

        @Test
        fun `activate throws when player status is invalid`() {
            val player = createTestGamePlayerSession(status = GamePlayerStatus.PENDING)
            session.requestJoin(player)

            shouldThrow<GamePlayerStateInvalidException> {
                session.activate(player.id)
            }
        }

        @Test
        fun `activate throws when player does not exist`() {
            shouldThrow<GamePlayerNotInSessionException> {
                session.activate(createTestGamePlayerId())
            }
        }
    }

    @Nested
    inner class Disconnect {
        @Test
        fun `disconnect changes player status from active to disconnected`() {
            val player = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
            session.requestJoin(player)

            session.disconnect(player.id)

            player.status shouldBe GamePlayerStatus.DISCONNECTED
        }

        @Test
        fun `disconnect throws when player status is invalid`() {
            val player = createTestGamePlayerSession(status = GamePlayerStatus.PENDING)
            session.requestJoin(player)

            shouldThrow<GamePlayerStateInvalidException> {
                session.disconnect(player.id)
            }
        }

        @Test
        fun `disconnect throws when player does not exist`() {
            shouldThrow<GamePlayerNotInSessionException> {
                session.disconnect(createTestGamePlayerId())
            }
        }
    }

    @Nested
    inner class TransitionToStarting {
        @Test
        fun `transition to starting changes state when current state is lobby`() {
            val startAt = Clock.System.now() + 10.seconds

            session.transitionToStarting(startAt)

            session.state shouldBe GameState.Starting(startAt)
        }

        @Test
        fun `transition to starting throws when current state is invalid`() {
            val startAt = Clock.System.now() + 10.seconds
            session.state = GameState.InProgress

            shouldThrow<GameStateInvalidException> {
                session.transitionToStarting(startAt)
            }
        }
    }

    @Nested
    inner class TransitionToInProgress {
        @Test
        fun `transition to in progress changes state when current state is starting`() {
            val startAt = Clock.System.now() + 10.seconds
            session.state = GameState.Starting(startAt)

            session.transitionToInProgress()

            session.state shouldBe GameState.InProgress
        }

        @Test
        fun `transition to in progress throws when current state is invalid`() {
            session.state = GameState.Lobby

            shouldThrow<GameStateInvalidException> {
                session.transitionToInProgress()
            }
        }
    }
}
