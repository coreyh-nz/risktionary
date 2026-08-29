package nz.coreyh.risktionary.game.socket.support

object WebSocketMappings {
    private const val BASE = "/game"

    const val CHAT = "$BASE/chat"
    const val RISK_RATING = "$BASE/risk-rating"
    const val VOLUNTEER = "$BASE/volunteer"
    const val UNVOLUNTEER = "$BASE/unvolunteer"
    const val SELECT_DRAWER = "$BASE/select-drawer"
}
