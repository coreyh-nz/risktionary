import { useWebSocket } from "@/providers/web-socket-provider"

export const useSkipCommand = () => {
  const { send } = useWebSocket()

  const skip = () => {
    send("/app/game/skip-phase")
  }

  return { skip }
}
