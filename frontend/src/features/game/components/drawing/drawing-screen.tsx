import { useGameRoundState, useGameSession, } from "../../stores/game-store-selectors"
import { DrawerPanel } from "./panel/drawer-panel"
import { SpectatorPanel } from "./panel/spectator-panel"

export const DrawingScreen = () => {
  const session = useGameSession()
  const roundState = useGameRoundState()

  if (roundState?.type !== "IN_PROGRESS") return

  const isDrawer =
    session?.role === "player" && session.playerId === roundState.drawerId

  return (
    <div className="mx-auto flex max-w-7xl flex-1">
      {isDrawer ? <DrawerPanel /> : <SpectatorPanel />}
    </div>
  )
}
