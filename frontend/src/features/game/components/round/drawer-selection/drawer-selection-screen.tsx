import { CenteredLayout } from "@/components/layout/centered-layout"
import { assertNever } from "@/lib/utils"
import { useGameSession } from "../../../stores/game-store-selectors"
import { HostDrawerSelectionScreen } from "./host-drawer-selection-screen"
import { PlayerDrawerSelectionScreen } from "./player-drawer-selection-screen"

export const DrawerSelectionScreen = () => {
  const session = useGameSession()
  if (!session) return null

  const render = () => {
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

  return <CenteredLayout size="3xl">{render()}</CenteredLayout>
}
