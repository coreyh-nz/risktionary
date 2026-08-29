package nz.coreyh.risktionary.game.socket.controller

import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameHostActionDispatcher
import nz.coreyh.risktionary.game.domain.model.action.GameRoundSelectDrawerAction
import nz.coreyh.risktionary.game.domain.model.action.GameRoundSkipPhaseAction
import nz.coreyh.risktionary.game.socket.messages.inbound.round.SelectDrawerCommand
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketMappings
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameHostSocketController(
    private val gameHostActionDispatcher: GameHostActionDispatcher,
) {
    @MessageMapping(WebSocketMappings.SELECT_DRAWER)
    fun onSelectDrawer(
        principal: GameSocketPrincipal,
        command: SelectDrawerCommand,
    ) {
        if (principal !is GameSocketPrincipal.Host) return

        gameHostActionDispatcher.dispatch(
            gameId = principal.gameId,
            hostId = principal.id,
            action = GameRoundSelectDrawerAction(command.drawerId),
        )
    }

    @MessageMapping(WebSocketMappings.SKIP_PHASE)
    fun onSkipPhase(principal: GameSocketPrincipal) {
        if (principal !is GameSocketPrincipal.Host) return

        gameHostActionDispatcher.dispatch(
            gameId = principal.gameId,
            hostId = principal.id,
            action = GameRoundSkipPhaseAction,
        )
    }
}
