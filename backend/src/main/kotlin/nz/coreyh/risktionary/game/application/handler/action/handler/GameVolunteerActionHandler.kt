package nz.coreyh.risktionary.game.application.handler.action.handler

import nz.coreyh.risktionary.game.application.handler.action.GameActionHandler
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.action.GameVolunteerAction
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameVolunteerActionHandler(
    private val gameEventPublisher: GameEventPublisher,
) : GameActionHandler<GameVolunteerAction> {
    override val actionClass = GameVolunteerAction::class

    override fun handle(
        session: GameSession,
        player: GamePlayerSession,
        action: GameVolunteerAction,
    ) {
        session.volunteers.volunteer(player.id)
        gameEventPublisher.publishVolunteersUpdated(session.id, session.volunteers.getVolunteers())
    }
}
