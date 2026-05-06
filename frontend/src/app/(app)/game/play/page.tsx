"use client"

import { GamePlayLobbyScreen } from "@/features/game/components/lobby/game-lobby-screen"
import { assertNever } from "@/lib/utils"
import { WebSocketProvider } from "@/providers/web-socket-provider"
import ConnectingScreen from "@/features/game/components/lobby/connecting-screen"
import { useGameState } from "@/features/game/stores/game-store-selectors"
import { useGameConnection } from "@/features/game/hooks/use-game-connection"
import { CenteredLayout } from "@/components/layout/centered-layout"

const GamePlayPage = () => {
  return (
    <WebSocketProvider>
      <GameScreen />
    </WebSocketProvider>
  )
}

const GameScreen = () => {
  const state = useGameState()
  const { connected, attempts } = useGameConnection()

  if (!connected) {
    return (
      <CenteredLayout>
        <ConnectingScreen attempts={attempts} />
      </CenteredLayout>
    )
  }

  const stateType = state.type
  switch (stateType) {
    case "INITIALIZING":
      return <p>INITIALIZING</p>
    case "LOBBY":
    case "STARTING":
      return (
        <CenteredLayout size="3xl">
          <GamePlayLobbyScreen />
        </CenteredLayout>
      )
    case "IN_PROGRESS":
      return <p>IN_PROGRESS</p>
    case "PAUSED":
      return <p>PAUSED</p>
    case "COMPLETED":
      return <p>COMPLETED</p>
    default:
      // type safe - makes the switch cause exhaustive
      assertNever(stateType)
  }
}
export default GamePlayPage
