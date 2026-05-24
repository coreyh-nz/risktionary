package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundState
import nz.coreyh.risktionary.game.application.session.round.requirePhase
import nz.coreyh.risktionary.game.application.session.round.requireState
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.words.domain.model.Word
import org.springframework.stereotype.Service

/**
 * Handles round-level operations within an active [GameSession].
 */
@Service
class GameRoundSessionService(
    private val gameSessionStore: GameSessionStore,
    private val gameEventPublisher: GameEventPublisher,
) {
    fun createRound(
        gameId: GameId,
        word: Word,
    ) {
        val session = getSession(gameId)
        val roundId = createRoundId()
        val round = GameRoundSession(id = roundId, gameId = gameId, word = word)
        session.currentRound = round

        gameEventPublisher.publishRoundStateChanged(gameId, round.state)
    }

    fun transitionToDrawingReviewAllGuessed(round: GameRoundSession) {
        transitionToDrawingReview(round)
        gameEventPublisher.publishRoundChatMessage(
            gameId = round.gameId,
            message = ChatMessage.System.DrawingEndedAllGuessed(round.word.value),
        )
    }

    fun transitionToDrawingReviewTimesUp(round: GameRoundSession) {
        transitionToDrawingReview(round)
        gameEventPublisher.publishRoundChatMessage(
            gameId = round.gameId,
            message = ChatMessage.System.DrawingEndedTimeUp(round.word.value),
        )
    }

    private fun transitionToDrawingReview(round: GameRoundSession) {
        val state = round.requireState<GameRoundState.InProgress>()
        state.requirePhase<GameRoundPhase.Drawing>()
        updateRoundPhase(round, GameRoundPhase.DrawingReview(word = round.word.value))
    }

    private fun updateRoundPhase(
        round: GameRoundSession,
        phase: GameRoundPhase,
    ) {
        val state = round.requireState<GameRoundState.InProgress>()
        updateRoundState(
            round = round,
            state = state.copy(phase = phase),
        )
    }

    private fun updateRoundState(
        round: GameRoundSession,
        state: GameRoundState,
    ) {
        round.state = state
        gameEventPublisher.publishRoundStateChanged(round.gameId, round.state)
    }

    private fun getSession(gameId: GameId): GameSession = gameSessionStore.findById(gameId) ?: throw GameNotFoundException()
}
