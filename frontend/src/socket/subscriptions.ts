import { Client } from "@stomp/stompjs"
import { setupPlayerSubscriptions } from "./subscriptions/player-subscriptions"

export const setupSubscriptions = (client: Client, gameId: string) => {
  setupPlayerSubscriptions(client, gameId)
}
