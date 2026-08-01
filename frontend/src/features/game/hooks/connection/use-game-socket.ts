import { useWebSocket } from "@/providers/web-socket-provider"

export const useGameSocket = () => {
  const { send } = useWebSocket()

  return {
    start: () => {
      send(`/app/game/start`)
    },
  }
}
