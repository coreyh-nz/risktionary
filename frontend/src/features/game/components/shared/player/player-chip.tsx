import { UserAvatar } from "@/components/common/user-avatar"
import { GameSessionPlayer } from "@/features/game/types/game"
import { cn } from "cn"

import { HTMLAttributes } from "react"

interface PlayerChipProps extends HTMLAttributes<HTMLDivElement> {
  player: GameSessionPlayer
  isMe: boolean
}

export const PlayerChip = ({ className, player, isMe }: PlayerChipProps) => {
  return (
    <div
      className={cn(
        "flex flex-col items-center gap-2 rounded-xl border border-border bg-card px-2 py-3",
        "animate-in duration-300 zoom-in-50 fade-in",
        "transition-colors hover:border-border/80 hover:bg-accent/40",
        className
      )}
    >
      <div className="relative">
        <UserAvatar displayName={player.displayName} />

        {isMe && (
          <span className="absolute -top-1 -right-1 translate-x-1/2 rounded-full bg-primary px-1 py-[1px] text-[10px] text-primary-foreground shadow">
            You
          </span>
        )}
      </div>
      <span className="max-w-18 truncate text-center text-xs leading-tight">
        {player.displayName}
      </span>
    </div>
  )
}
