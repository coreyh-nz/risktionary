import { Stack } from "@/components/layout/stack"
import { RiskRatingResultsDensityMatrix } from "@/features/game/components/round/phase/rating/risk-rating-results-density-matrix"
import { ScreenHeader } from "@/features/game/components/screen-header"
import { ChartColumn } from "lucide-react"

export const RiskRatingResultsScreen = () => {
  return (
    <Stack>
      <ScreenHeader
        icon={ChartColumn}
        label="Risk Rating Results"
        title="How the room voted"
      />

      <RiskRatingResultsDensityMatrix />
    </Stack>
  )
}
