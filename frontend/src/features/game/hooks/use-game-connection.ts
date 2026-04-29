import { useGameSession } from "@/features/game/stores/game-store-selectors"
import { useWebSocket } from "@/providers/web-socket-provider"
import { useRouter } from "next/navigation"
import { useEffect } from "react"
import { config } from "@/lib/config"
import { setupSubscriptions } from "@/features/game/socket/subscriptions"
import { toast } from "sonner"
import { ROUTES } from "@/lib/routes"

export const useGameConnection = () => {
  const session = useGameSession()
  const { connect, disconnect, connected, attempts, error } = useWebSocket()
  const router = useRouter()

  useEffect(() => {
    if (session) {
      const url =
        session.role === "player"
          ? `${config.wsUrl}?ticket=${session.ticket}`
          : config.wsUrl
      connect(url, (client) => setupSubscriptions(client, session.gameId))
    }
    return () => {
      disconnect()
    }
  }, [session, connect, disconnect])

  useEffect(() => {
    if (!error) return
    toast.error(error)
    router.push(ROUTES.HOME)
  }, [error, router])

  return { connected, attempts, error }
}
