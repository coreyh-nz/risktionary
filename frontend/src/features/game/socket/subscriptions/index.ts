import { Client } from "@stomp/stompjs"
import { setupPlayerSubscriptions } from "./player-subscriptions"

export const setupSubscriptions = (client: Client, gameId: string) => {
  setupPlayerSubscriptions(client, gameId)

  // let the server know we have subscribed to every we need to
  // and are ready to start receiving messages (and get initial data)
  client.publish({
    destination: "/app/player/ready",
    body: JSON.stringify({ gameId: gameId }),
  })
}
