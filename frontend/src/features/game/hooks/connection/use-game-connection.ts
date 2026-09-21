import { setupSubscriptions } from "@/features/game/socket/subscriptions"
import { useGameSession } from "@/features/game/stores/game-store-selectors"
import { config } from "@/lib/config"
import { ROUTES } from "@/lib/routes"
import { useWebSocket, WebSocketError } from "@/providers/web-socket-provider"
import { useRouter } from "next/navigation"
import { useEffect } from "react"
import { toast } from "sonner"

const ERROR_MESSAGES: Record<string, string> = {
  "game.not-found": "That game doesn't exist or has ended.",
  "game.ticket-invalid": "Your join link has expired. Please rejoin the game.",
  "game.player-not-in-session": "You haven't joined this game.",
  "auth.unauthenticated": "Please sign in to host a game.",
  "generic.bad-request": "Couldn't connect to the game.",
  "client.connection-failed": "Couldn't reach the server. Please try again.",
}

const getErrorMessage = (error: WebSocketError) =>
  ERROR_MESSAGES[error.code] ?? error.message

export const useGameConnection = () => {
  const session = useGameSession()
  const { connect, disconnect, connected, attempts, error, clearError } =
    useWebSocket()
  const router = useRouter()

  useEffect(() => {
    if (session) {
      const url =
        session.role === "player"
          ? `${config.wsUrl}?ticket=${session.ticket}`
          : `${config.wsUrl}?gameId=${session.gameId}`
      connect(url, (client) => setupSubscriptions(client, session.gameId))
    }
    return () => {
      disconnect()
    }
  }, [session, connect, disconnect])

  useEffect(() => {
    if (!error) return
    toast.error(getErrorMessage(error), { id: `ws-error:${error.code}` })
    router.push(ROUTES.HOME)
    clearError()
  }, [error, router, clearError])

  return { connected, attempts, error }
}
