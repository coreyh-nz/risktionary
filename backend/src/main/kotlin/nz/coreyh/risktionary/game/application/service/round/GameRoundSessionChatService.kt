package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.game.socket.messages.view.toView
import org.springframework.stereotype.Service

@Service
class GameRoundSessionChatService(
    private val gameEventPublisher: GameEventPublisher,
) {
    fun sendMessage(
        round: GameRoundSession,
        message: ChatMessage,
    ) {
        round.addMessage(message)
        gameEventPublisher.publishRoundChatMessage(
            gameId = round.game.id,
            message = message,
        )
    }

    fun sendPlayerMessage(
        round: GameRoundSession,
        player: GamePlayerSession,
        text: String,
    ) = sendMessage(
        round,
        ChatMessage.Player(
            player = player.player.toView(),
            text = text,
        ),
    )
}
