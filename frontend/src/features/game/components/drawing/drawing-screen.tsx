import {
  useGameRoundState,
  useGameSession,
} from "../../stores/game-store-selectors"
import { RoundWordView } from "../round/round-word-view"
import { DrawerPanel } from "./panel/drawer-panel"
import { SpectatorPanel } from "./panel/spectator-panel"

export const DrawingScreen = () => {
  const session = useGameSession()
  const roundState = useGameRoundState()

  if (roundState?.type !== "IN_PROGRESS") return

  const isDrawer =
    session?.role === "player" && session.playerId === roundState.drawerId

  return (
    <div className="mx-auto flex flex-col max-w-7xl flex-1">
      <div className="flex justify-center">
        <RoundWordView />
      </div>
      {isDrawer ? <DrawerPanel /> : <SpectatorPanel />}
    </div>
  )
}
