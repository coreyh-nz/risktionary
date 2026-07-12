package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.requireActiveRound
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameRoundSessionChatService(
    private val gameRoundSessionGuessService: GameRoundSessionGuessService,
    private val gameEventPublisher: GameEventPublisher,
) {
    fun handleChat(
        session: GameSession,
        player: GamePlayerSession,
        text: String,
    ) {
        val round = session.requireActiveRound()
        val roundState = round.requireState<GameRoundState.InProgress>()
        val drawerId = roundState.drawer.id
        val hasCorrectlyGuessed = round.guesses.hasGuessedCorrectly(player.id)
        val isDrawer = drawerId == player.id

        when {
            hasCorrectlyGuessed || isDrawer -> {
                sendPlayerMessage(round, player, text)
            }

            else -> {
                when (gameRoundSessionGuessService.handleGuess(round, player, text)) {
                    GuessResultType.INCORRECT -> {
                        // treat as a normal chat message
                        sendPlayerMessage(round, player, text)
                    }

                    GuessResultType.CORRECT -> {
                        sendMessage(
                            round,
                            ChatMessage.System.CorrectGuess(
                                playerId = player.id,
                                playerDisplayName = player.player.identity.displayName,
                            ),
                        )

                        // placed in here instead of directly in guess service to prevent the "all guessed" message
                        // being sent before the individual player correct guess message
                        gameRoundSessionGuessService.checkRoundCompletion(session, round)
                    }
                }
            }
        }
    }

    private fun sendPlayerMessage(
        round: GameRoundSession,
        player: GamePlayerSession,
        text: String,
    ) = sendMessage(
        round,
        ChatMessage.Player(
            playerId = player.id,
            playerDisplayName = player.player.identity.displayName,
            text = text,
        ),
    )

    private fun sendMessage(
        round: GameRoundSession,
        message: ChatMessage,
    ) {
        round.addMessage(message)
        gameEventPublisher.publishRoundChatMessage(
            gameId = round.game.id,
            message = message,
        )
    }
}
