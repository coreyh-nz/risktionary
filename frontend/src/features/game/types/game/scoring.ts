export interface ScoreboardEntry {
  playerId: string
  displayName: string
  roundPoints: number
  totalPoints: number
  rank: number
}

export interface StandingEntry {
  playerId: string
  displayName: string
  totalPoints: number
  rank: number
}
