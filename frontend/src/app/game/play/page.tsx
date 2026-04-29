"use client"

import { GamePlayLobbyScreen } from "@/features/lobby/components/game-lobby-screen"
import { useGameStore } from "@/stores/game-store"
import { assertNever } from "@/lib/utils"
import { useWebSocket, WebSocketProvider } from "@/context/web-socket-context"
import ConnectingScreen from "@/features/lobby/components/connecting-screen"
import { useRouter } from "next/navigation"
import { ROUTES } from "@/lib/routes"
import { toast } from "sonner"
import { useEffect } from "react"
import { setupSubscriptions } from "@/socket/subscriptions"

const GamePlayPage = () => {
  const { session } = useGameStore()

  if (!session) {
    return <div>no session</div>
  }

  return (
    <WebSocketProvider>
      <GameScreen />
    </WebSocketProvider>
  )
}

const GameScreen = () => {
  const { session, state } = useGameStore()
  const { connect, disconnect, connected, attempts, error } = useWebSocket()
  const router = useRouter()

  useEffect(() => {
    if (session) {
      connect((client) => setupSubscriptions(client, session.gameId))
    }
    return () => disconnect()
  }, [session, connect, disconnect])

  useEffect(() => {
    if (!error) return
    toast.error(error)
    router.push(ROUTES.HOME)
  }, [error, router])

  if (!connected && !error) {
    return <ConnectingScreen attempts={attempts} />
  }

  switch (state) {
    case "INITIALIZING":
      return <p>INITIALIZING</p>
    case "LOBBY":
      return <GamePlayLobbyScreen />
    case "STARTING":
      return <p>STARTING</p>
    case "IN_PROGRESS":
      return <p>IN_PROGRESS</p>
    case "PAUSED":
      return <p>PAUSED</p>
    case "COMPLETED":
      return <p>COMPLETED</p>
    default:
      assertNever(state)
  }
}
export default GamePlayPage
