import { CenteredLayout } from "@/components/layout/centered-layout"
import { GAME_STATE_COMPONENTS } from "@/features/game/components/game-state-registry"
import ConnectingScreen from "@/features/game/components/lobby/connecting-screen"
import { useGameConnection } from "@/features/game/hooks/connection/use-game-connection"
import { useGameState } from "@/features/game/stores/game-store-selectors"

export const GameScreen = () => {
  const { type: stateType } = useGameState()
  const { connected, attempts } = useGameConnection()

  if (!connected) {
    return (
      <CenteredLayout>
        <ConnectingScreen attempts={attempts} />
      </CenteredLayout>
    )
  }

  const GameStateComponent = GAME_STATE_COMPONENTS[stateType]
  return <GameStateComponent />
}
