package nz.coreyh.risktionary.unit.game.application.session

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.game.application.exception.GamePlayerAlreadyInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerNotInSessionException
import nz.coreyh.risktionary.game.application.exception.GamePlayerStateInvalidException
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameSessionTests {
    private lateinit var session: GameSession
    private val gameId = createTestGameId()
    private val hostId = createTestUserId()
    private val code = "ABC123"

    @BeforeEach
    fun setup() {
        session = GameSession(gameId, hostId, code)
    }

    @Test
    fun `request join adds player when player is not in session`() {
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

    @Test
    fun `connect sets status to connecting when player is pending`() {
        val player = createTestGamePlayerSession()
        session.requestJoin(player)

        session.connect(player.id)

        player.status shouldBe GamePlayerStatus.CONNECTING
    }

    @Test
    fun `connect sets status to connecting when player is disconnected`() {
        val player = createTestGamePlayerSession()
        session.requestJoin(player)
        session.connect(player.id)
        session.activate(player.id)
        session.disconnect(player.id)

        session.connect(player.id)

        player.status shouldBe GamePlayerStatus.CONNECTING
    }

    @Test
    fun `connect throws when player is active`() {
        val player = createTestGamePlayerSession()
        session.requestJoin(player)
        session.connect(player.id)
        session.activate(player.id)

        shouldThrow<GamePlayerStateInvalidException> {
            session.connect(player.id)
        }
    }

    @Test
    fun `connect throws when player does not exist`() {
        val playerId = createTestGamePlayerId()
        shouldThrow<GamePlayerNotInSessionException> {
            session.connect(playerId)
        }
    }

    @Test
    fun `activate sets status to active when player is connecting`() {
        val player = createTestGamePlayerSession()
        session.requestJoin(player)
        session.connect(player.id)

        session.activate(player.id)

        player.status shouldBe GamePlayerStatus.ACTIVE
    }

    @Test
    fun `activate throws when player is pending`() {
        val player = createTestGamePlayerSession()
        session.requestJoin(player)

        shouldThrow<GamePlayerStateInvalidException> {
            session.activate(player.id)
        }
    }

    @Test
    fun `activate throws when player does not exist`() {
        val playerId = createTestGamePlayerId()

        shouldThrow<GamePlayerNotInSessionException> {
            session.activate(playerId)
        }
    }

    @Test
    fun `disconnect sets status to disconnected when player is active`() {
        val player = createTestGamePlayerSession()
        session.requestJoin(player)
        session.connect(player.id)
        session.activate(player.id)

        session.disconnect(player.id)

        player.status shouldBe GamePlayerStatus.DISCONNECTED
    }

    @Test
    fun `disconnect throws when player is not active`() {
        val player = createTestGamePlayerSession()
        session.requestJoin(player)

        shouldThrow<GamePlayerStateInvalidException> {
            session.disconnect(player.id)
        }
    }

    @Test
    fun `disconnect throws when player does not exist`() {
        val playerId = createTestGamePlayerId()

        shouldThrow<GamePlayerNotInSessionException> {
            session.disconnect(playerId)
        }
    }
}
