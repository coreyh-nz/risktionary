import { CenteredLayout } from "@/components/layout/centered-layout"
import { DrawingScreen } from "@/features/game/components/drawing/drawing-screen"
import { assertNever } from "@/lib/utils"
import { useGameRoundState } from "../stores/game-store-selectors"
import { DrawerSelectionScreen } from "./drawer-selection/screen/drawer-selection-screen"

export const GamePlayScreen = () => {
  const roundState = useGameRoundState()
  if (!roundState) return

  switch (roundState.type) {
    case "SELECTING_DRAWER": {
      return (
        <CenteredLayout size="3xl">
          <DrawerSelectionScreen />
        </CenteredLayout>
      )
    }
    case "IN_PROGRESS": {
      return <DrawingScreen />
    }
    case "COMPLETED": {
      return <p>COMPLETED</p>
    }
    default:
      assertNever(roundState)
  }
}
