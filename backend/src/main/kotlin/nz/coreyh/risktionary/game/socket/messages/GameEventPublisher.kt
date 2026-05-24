package nz.coreyh.risktionary.game.socket.messages

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.GameState
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.hint.WordHint
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerJoinedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerLeftEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerListUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.StateChangedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.VolunteersUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.ChatMessageEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundAssignedDrawerEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundAssignedGuesserEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundCorrectGuessEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundCorrectGuessesCountUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundStateChangedEvent
import nz.coreyh.risktionary.game.socket.messages.view.toView
import nz.coreyh.risktionary.game.socket.support.WebSocketDestinations
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.domain.model.Word
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

    fun publishStateChanged(
        gameId: GameId,
        gameState: GameState,
    ) = messagingTemplate.sendToTopic(
        destination = WebSocketDestinations.Topic.base(gameId),
        message = StateChangedEvent(gameState),
    )

    fun publishVolunteersUpdated(
        gameId: GameId,
        volunteers: List<GamePlayerId>,
    ) = messagingTemplate.sendToTopic(
        destination = WebSocketDestinations.Topic.base(gameId),
        message = VolunteersUpdatedEvent(volunteers),
    )

    fun publishRoundStateChanged(
        gameId: GameId,
        roundState: GameRoundState,
    ) = messagingTemplate.sendToTopic(
        destination = WebSocketDestinations.Topic.base(gameId),
        message = RoundStateChangedEvent(roundState),
    )

    fun publishAssignedDrawerEvent(
        playerId: GamePlayerId,
        word: String,
    ) = messagingTemplate.sendToPlayer(
        playerId = playerId,
        destination = WebSocketDestinations.Queue.ROUND,
        message = RoundAssignedDrawerEvent(word),
    )

    fun publishAssignedGuesserEvent(
        playerId: GamePlayerId,
        hint: WordHint,
    ) {
        messagingTemplate.sendToPlayer(
            playerId = playerId,
            destination = WebSocketDestinations.Queue.ROUND,
            message = RoundAssignedGuesserEvent(hint),
        )
    }

    fun publishAssignedGuesserEvent(
        userId: UserId,
        hint: WordHint,
    ) {
        messagingTemplate.sendToUser(
            userId = userId,
            destination = WebSocketDestinations.Queue.ROUND,
            message = RoundAssignedGuesserEvent(hint),
        )
    }

    fun publishRoundChatMessage(
        gameId: GameId,
        message: ChatMessage,
    ) {
        messagingTemplate.sendToTopic(
            destination = WebSocketDestinations.Topic.round(gameId),
            message = ChatMessageEvent(message),
        )
    }

    fun publishRoundCorrectGuess(
        playerId: GamePlayerId,
        word: Word,
    ) {
        messagingTemplate.sendToPlayer(
            playerId = playerId,
            destination = WebSocketDestinations.Queue.ROUND,
            message = RoundCorrectGuessEvent(word.value),
        )
    }

    fun publishRoundCorrectGuessesCountUpdated(
        gameId: GameId,
        correctGuesses: Int,
    ) {
        messagingTemplate.sendToTopic(
            destination = WebSocketDestinations.Topic.round(gameId),
            message = RoundCorrectGuessesCountUpdatedEvent(correctGuesses),
        )
    }
}

private inline fun <reified T : Any> SimpMessagingTemplate.sendToTopic(
    destination: String,
    message: T,
) {
    kLogger.debug {
        "Publishing ${message.javaClass.simpleName} [broadcast] → $destination"
    }
    convertAndSend(destination, message)
}

private inline fun <reified T : Any> SimpMessagingTemplate.sendToPlayer(
    playerId: GamePlayerId,
    destination: String,
    message: T,
) {
    kLogger.debug {
        "Publishing ${message.javaClass.simpleName} [playerId=$playerId] -> $destination"
    }
    convertAndSendToUser(playerId.value.toString(), destination, message)
}

private inline fun <reified T : Any> SimpMessagingTemplate.sendToUser(
    userId: UserId,
    destination: String,
    message: T,
) {
    kLogger.debug {
        "Publishing ${message.javaClass.simpleName} [userId=$userId] -> $destination"
    }
    convertAndSendToUser(userId.value.toString(), destination, message)
}
