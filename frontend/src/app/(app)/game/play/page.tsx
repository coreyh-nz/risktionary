"use client"

import { CenteredLayout } from "@/components/layout/centered-layout"
import { FullPageLayout } from "@/components/layout/full-page-layout"
import { GamePlayScreen } from "@/features/game/components/game-play-screen"
import ConnectingScreen from "@/features/game/components/lobby/connecting-screen"
import { GamePlayLobbyScreen } from "@/features/game/components/lobby/game-lobby-screen"
import { useGameConnection } from "@/features/game/hooks/use-game-connection"
import { useGameState } from "@/features/game/stores/game-store-selectors"
import { assertNever } from "@/lib/utils"
import { WebSocketProvider } from "@/providers/web-socket-provider"

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
      return (
        <FullPageLayout>
          <GamePlayScreen />
        </FullPageLayout>
      )
    case "COMPLETED":
      return <p>COMPLETED</p>
    default:
      // type safe - makes the switch cause exhaustive
      assertNever(stateType)
  }
}
export default GamePlayPage
