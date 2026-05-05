package nz.coreyh.risktionary.game.socket.messages

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerJoinedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerLeftEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerListUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.view.toView
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import nz.coreyh.risktionary.user.domain.model.UserId
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

private val kLogger = KotlinLogging.logger {}

@Component
class GameEventPublisher(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    fun publishPlayerJoined(
        gameId: GameId,
        player: GamePlayerSession,
    ) = messagingTemplate.sendToTopic(
        destination = WebSocketDestinations.Topic.players(gameId),
        message = PlayerJoinedEvent(player.toView()),
    )

    fun publishPlayerLeft(
        gameId: GameId,
        playerId: GamePlayerId,
    ) = messagingTemplate.sendToTopic(
        destination = WebSocketDestinations.Topic.players(gameId),
        message = PlayerLeftEvent(playerId),
    )

    fun publishPlayerList(
        playerId: GamePlayerId,
        players: List<GamePlayerSession>,
    ) = messagingTemplate.sendToPlayer(
        playerId = playerId,
        destination = WebSocketDestinations.Queue.PLAYER_LIST,
        message =
            PlayerListUpdatedEvent(
                players =
                    players
                        .filter { it.status == GamePlayerStatus.ACTIVE }
                        .map { it.toView() },
            ),
    )
}

private inline fun <reified T : Any> SimpMessagingTemplate.sendToTopic(
    destination: String,
    message: T,
) {
    kLogger.debug {
        "Publishing ${T::class.simpleName} [broadcast] → $destination"
    }
    convertAndSend(destination, message)
}

private inline fun <reified T : Any> SimpMessagingTemplate.sendToPlayer(
    playerId: GamePlayerId,
    destination: String,
    message: T,
) {
    kLogger.debug {
        "Publishing ${T::class.simpleName} [playerId=$playerId] -> $destination"
    }
    convertAndSendToUser(playerId.value.toString(), destination, message)
}

private inline fun <reified T : Any> SimpMessagingTemplate.sendToUser(
    userId: UserId,
    destination: String,
    message: T,
) {
    kLogger.debug {
        "Publishing ${T::class.simpleName} [userId=$userId] -> $destination"
    }
    convertAndSendToUser(userId.value.toString(), destination, message)
}
