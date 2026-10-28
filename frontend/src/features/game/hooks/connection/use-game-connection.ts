import { useGameHydrated } from "@/features/game/hooks/connection/use-game-hydrated"
import { setupSubscriptions } from "@/features/game/socket/subscriptions"
import {
  useGameReset,
  useGameSession,
} from "@/features/game/stores/game-store-selectors"
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
  const hydrated = useGameHydrated()
  const reset = useGameReset()
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

  // e.g. opening /game/play directly, or after the session was cleared
  useEffect(() => {
    if (hydrated && !session) router.replace(ROUTES.HOME)
  }, [hydrated, session, router])

  useEffect(() => {
    if (!error) return
    toast.error(getErrorMessage(error), { id: `ws-error:${error.code}` })
    // a refused session is persisted, so drop it or every refresh would retry
    // it. A failed connection may be temporary, so that one is kept.
    if (error.code !== "client.connection-failed") reset()
    router.push(ROUTES.HOME)
    clearError()
  }, [error, router, clearError, reset])

  return { connected, attempts, error }
}
