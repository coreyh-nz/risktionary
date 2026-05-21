import { assertNever } from "@/lib/utils"
import { useGameSession } from "../../../stores/game-store-selectors"
import { HostDrawerSelectionScreen } from "./host-drawer-selection-screen"
import { PlayerDrawerSelectionScreen } from "./player-drawer-selection-screen"

export const DrawerSelectionScreen = () => {
  const session = useGameSession()
  if (!session) return null

  const role = session.role
  switch (role) {
    case "host":
      return <HostDrawerSelectionScreen />
    case "player":
      return <PlayerDrawerSelectionScreen />
    default:
      assertNever(role)
  }
}
