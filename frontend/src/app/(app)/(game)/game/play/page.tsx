"use client"

import { GamePlayLobbyScreen } from "@/features/game/components/lobby/game-lobby-screen"
import { assertNever } from "@/lib/utils"
import { WebSocketProvider } from "@/providers/web-socket-provider"
import ConnectingScreen from "@/features/game/components/lobby/connecting-screen"
import { useGamePhase } from "@/features/game/stores/game-store-selectors"
import { useGameConnection } from "@/features/game/hooks/use-game-connection"

const GamePlayPage = () => {
  return (
    <WebSocketProvider>
      <GameScreen />
    </WebSocketProvider>
  )
}

const GameScreen = () => {
  const phase = useGamePhase()

  const { connected, attempts } = useGameConnection()

  if (!connected) {
    return <ConnectingScreen attempts={attempts} />
  }
  if (!phase) return null

  switch (phase) {
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
      // type safe - makes the switch cause exhaustive
      assertNever(phase)
  }
}
export default GamePlayPage
