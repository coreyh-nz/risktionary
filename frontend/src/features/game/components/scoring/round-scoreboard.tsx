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
      primary: `${e.totalPoints.toLocaleString()} pts`,
      secondary: `+${e.roundPoints.toLocaleString()} this round`,
      muted: e.roundPoints === 0,
    }))}
  />
)
