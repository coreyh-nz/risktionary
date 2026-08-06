package nz.coreyh.risktionary.game.application.handler.action.handler

import nz.coreyh.risktionary.game.application.handler.action.GameActionHandler
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.action.GameUnvolunteerAction
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameUnvolunteerActionHandler(
    private val gameEventPublisher: GameEventPublisher,
) : GameActionHandler<GameUnvolunteerAction> {
    override val actionClass = GameUnvolunteerAction::class

    override fun handle(
        session: GameSession,
        player: GamePlayerSession,
        action: GameUnvolunteerAction,
    ) {
        session.volunteers.unvolunteer(player.id)
        gameEventPublisher.publishVolunteersUpdated(session.id, session.volunteers.getVolunteers())
    }
}
