import { useGameSession } from "../../stores/game-store-selectors"
import { DrawerPanel } from "./panel/drawer-panel"
import { SpectatorPanel } from "./panel/spectator-panel"

export const DrawingScreen = () => {
  const session = useGameSession()

  // TODO: replace with actual player - assume host is not drawer
  const isDrawer = session?.role === "player"

  return (
    <div className="mx-auto flex max-w-7xl flex-1">
      {isDrawer ? <DrawerPanel /> : <SpectatorPanel />}
    </div>
  )
}
