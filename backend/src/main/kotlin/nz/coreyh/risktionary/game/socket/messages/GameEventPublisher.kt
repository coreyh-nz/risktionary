package nz.coreyh.risktionary.game.socket.messages

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.domain.model.risk.RiskRatingCount
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.hint.WordHint
import nz.coreyh.risktionary.game.socket.messages.outbound.GameStateEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerJoinedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerLeftEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.PlayerListUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.VolunteersUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.ChatMessageEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundAssignedDrawerEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundAssignedGuesserEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundCorrectGuessEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundCorrectGuessesCountUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundPhaseStateEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundRiskRatingsUpdatedEvent
import nz.coreyh.risktionary.game.socket.messages.outbound.round.RoundStateEvent
import nz.coreyh.risktionary.game.socket.messages.view.toPhaseStateView
import nz.coreyh.risktionary.game.socket.messages.view.toStateView
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
        destination = WebSocketDestinations.Topic.base(gameId),
        message = PlayerJoinedEvent(player.player.toView()),
    )

    fun publishPlayerLeft(
        gameId: GameId,
        playerId: GamePlayerId,
    ) = messagingTemplate.sendToTopic(
        destination = WebSocketDestinations.Topic.base(gameId),
        message = PlayerLeftEvent(playerId),
    )

    fun publishPlayerList(
        playerId: GamePlayerId,
        players: List<GamePlayerSession>,
    ) = messagingTemplate.sendToPlayer(
        playerId = playerId,
        destination = WebSocketDestinations.Queue.GAME,
        message =
            PlayerListUpdatedEvent(
                players =
                    players
                        .filter { it.status == GamePlayerStatus.ACTIVE }
                        .map { it.player.toView() },
            ),
    )

    fun publishVolunteersUpdated(
        gameId: GameId,
        volunteers: List<GamePlayerId>,
    ) = messagingTemplate.sendToTopic(
        destination = WebSocketDestinations.Topic.base(gameId),
        message = VolunteersUpdatedEvent(volunteers),
    )

    fun publishState(game: GameSession) {
        messagingTemplate.sendToTopic(
            destination = WebSocketDestinations.Topic.base(game.id),
            message = GameStateEvent(game.toStateView()),
        )
    }

    fun publishStateToPlayer(
        playerId: GamePlayerId,
        game: GameSession,
    ) {
        messagingTemplate.sendToPlayer(
            playerId = playerId,
            destination = WebSocketDestinations.Queue.GAME,
            message = GameStateEvent(game.toStateView()),
        )
    }

    fun publishRoundState(round: GameRoundSession) {
        messagingTemplate.sendToTopic(
            destination = WebSocketDestinations.Topic.round(round.game.id),
            message = RoundStateEvent(round.toStateView()),
        )
    }

    fun publishRoundPhaseState(round: GameRoundSession) {
        messagingTemplate.sendToTopic(
            destination = WebSocketDestinations.Topic.round(round.game.id),
            message = RoundPhaseStateEvent(round.toPhaseStateView()),
        )
    }

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

    fun publishRiskRatingsUpdatedToPlayer(
        playerId: GamePlayerId,
        counts: List<RiskRatingCount>,
    ) = messagingTemplate.sendToPlayer(
        playerId = playerId,
        destination = WebSocketDestinations.Queue.ROUND,
        message = RoundRiskRatingsUpdatedEvent(counts),
    )

    fun publishRiskRatingsUpdatedToHost(
        userId: UserId,
        counts: List<RiskRatingCount>,
    ) = messagingTemplate.sendToUser(
        userId = userId,
        destination = WebSocketDestinations.Queue.ROUND,
        message = RoundRiskRatingsUpdatedEvent(counts),
    )
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
