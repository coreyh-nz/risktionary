import { CenteredLayout } from "@/components/layout/centered-layout"
import { ScreenHeader } from "@/features/game/components/screen-header"
import { FinalStandings } from "@/features/game/components/scoring/final-standings"
import { useGameState } from "@/features/game/stores/game-store-selectors"
import { Trophy } from "lucide-react"

export const GameCompletedScreen = () => {
  const state = useGameState()
  if (state.type !== "COMPLETED") return null

  return (
    <CenteredLayout size="3xl">
      <div className="flex w-full flex-col gap-4">
        <ScreenHeader icon={Trophy} label="Game over" title="Final standings" />
        <FinalStandings standings={state.standings} />
      </div>
    </CenteredLayout>
  )
}
