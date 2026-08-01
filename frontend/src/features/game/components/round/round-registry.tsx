import { DrawerSelectionScreen } from "@/features/game/components/round/drawer-selection/drawer-selection-screen"
import { RoundCompletedScreen } from "@/features/game/components/round/round-completed-screen"
import { RoundInProgressScreen } from "@/features/game/components/round/round-in-progress-screen"
import { RoundState } from "@/features/game/types/round/phase/round"
import { ComponentType } from "react"

export type RoundStateType = RoundState["type"]

export const ROUND_COMPONENTS: Record<RoundStateType, ComponentType> = {
  SELECTING_DRAWER: DrawerSelectionScreen,
  IN_PROGRESS: RoundInProgressScreen,
  COMPLETED: RoundCompletedScreen,
}
