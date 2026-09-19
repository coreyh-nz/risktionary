import { Stack } from "@/components/layout/stack"
import { RiskRatingVoteForm } from "@/features/game/components/round/phase/rating/risk-rating-vote-form"
import { ScreenHeader } from "@/features/game/components/screen-header"
import { Shapes } from "lucide-react"

export const RiskRatingVoteScreen = () => {
  return (
    <Stack>
      <ScreenHeader
        icon={Shapes}
        label="Cast your vote"
        title="How likely you think this risk is and how severe its impact would be?"
      />

      <RiskRatingVoteForm />
    </Stack>
  )
}
