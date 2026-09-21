import { ScoreView } from "@/features/game/components/scoring/score-view"
import { StandingEntry } from "@/features/game/types/game/scoring"

export const FinalStandings = ({
  standings,
}: {
  standings?: StandingEntry[]
}) => (
  <ScoreView
    items={standings?.map((e) => ({
      playerId: e.playerId,
      displayName: e.displayName,
      rank: e.rank,
      primary: `${e.totalPoints.toLocaleString()} pts`,
    }))}
  />
)
