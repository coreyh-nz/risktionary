import { PlayerChip } from "./player-chip"
import {
  useGamePlayers,
  useGameSession,
} from "@/features/game/stores/game-store-selectors"

export const PlayerList = () => {
  const session = useGameSession()
  const players = useGamePlayers()
  if (!session) return null

  const isMe = (id: string) =>
    session.role === "player" && session.playerId === id

  return (
    <div className="mb-3 space-y-3">
      {/* Player Count */}
      <div className="flex items-center justify-between">
        <p className="text-xs font-semibold tracking-widest text-muted-foreground uppercase">
          {players.length} players joined
        </p>
      </div>

      {/* Player Chips */}
      <div className="grid grid-cols-[repeat(auto-fill,minmax(8rem,1fr))] gap-2">
        {players.map((player) => (
          <PlayerChip key={player.id} player={player} isMe={isMe(player.id)} />
        ))}
      </div>
    </div>
  )
}
