import { ScoreView } from "@/features/game/components/scoring/score-view"
import { ScoreboardEntry } from "@/features/game/types/game/scoring"

export const RoundScoreboard = ({
  scoreboard,
}: {
  scoreboard?: ScoreboardEntry[]
}) => (
  <ScoreView
    items={scoreboard?.map((e) => ({
      playerId: e.playerId,
      displayName: e.displayName,
      rank: e.rank,
      primary: `+${e.roundPoints.toLocaleString()}`,
      secondary: `${e.totalPoints.toLocaleString()} pts total`,
      muted: e.roundPoints === 0,
    }))}
  />
)
