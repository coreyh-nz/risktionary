package nz.coreyh.risktionary.game.domain.model.round.chat

import nz.coreyh.risktionary.game.socket.messages.view.GamePlayerView

sealed interface ChatMessage {
    val type: ChatMessageType

    sealed interface System : ChatMessage {
        val kind: ChatMessageSystemType

        data class DrawerSelected(
            val player: GamePlayerView,
        ) : System {
            override val type: ChatMessageType = ChatMessageType.SYSTEM
            override val kind: ChatMessageSystemType = ChatMessageSystemType.DRAWER_SELECTED
        }

        data class CorrectGuess(
            val player: GamePlayerView,
        ) : System {
            override val type: ChatMessageType = ChatMessageType.SYSTEM
            override val kind: ChatMessageSystemType = ChatMessageSystemType.PLAYER_GUESSED_CORRECTLY
        }

        data class DrawingEndedAllGuessed(
            val word: String,
        ) : System {
            override val type: ChatMessageType = ChatMessageType.SYSTEM
            override val kind: ChatMessageSystemType = ChatMessageSystemType.DRAWING_ENDED_ALL_GUESSED
        }

        data class DrawingEndedTimeUp(
            val word: String,
        ) : System {
            override val type: ChatMessageType = ChatMessageType.SYSTEM
            override val kind: ChatMessageSystemType = ChatMessageSystemType.DRAWING_ENDED_TIME_UP
        }
    }

    data class Player(
        val player: GamePlayerView,
        val text: String,
    ) : ChatMessage {
        override val type: ChatMessageType = ChatMessageType.PLAYER
    }
}
