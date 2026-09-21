import { UserAvatar } from "@/components/common/user-avatar"
import { useGameSession } from "@/features/game/stores/game-store-selectors"
import { cn } from "cn"
import { Crown } from "lucide-react"

// Rows shown under the podium. Anyone below this is not listed; the current
// player is pinned at the bottom instead.
const MAX_LIST_ROWS = 5
const PODIUM_SIZE = 3

export interface ScoreItem {
  playerId: string
  displayName: string
  rank: number
  // shown on the podium and at the right of each row
  primary: string
  // shown under the name in the list
  secondary?: string
  muted?: boolean
}

const motion =
  "motion-safe:animate-in motion-safe:fade-in motion-safe:fill-mode-backwards motion-safe:duration-500"

const PODIUM_STYLE: Record<number, { bar: string; height: string }> = {
  1: { bar: "bg-chart-4 text-white", height: "h-36" },
  2: { bar: "bg-chart-2 text-slate-950", height: "h-28" },
  3: { bar: "bg-chart-1 text-slate-950", height: "h-20" },
}
const PODIUM_DEFAULT = { bar: "bg-muted text-foreground", height: "h-16" }

// Kahoot style: the lowest step is revealed first
const revealDelay = (position: number) => [600, 300, 0][position] ?? 0

const PodiumStep = ({
  item,
  position,
  isMe,
}: {
  item: ScoreItem
  position: number
  isMe: boolean
}) => {
  const style = PODIUM_STYLE[item.rank] ?? PODIUM_DEFAULT
  return (
    <div
      className={cn(
        "flex w-24 flex-col items-center sm:w-32",
        motion,
        "motion-safe:slide-in-from-bottom-6"
      )}
      style={{ animationDelay: `${revealDelay(position)}ms` }}
    >
      <div className="relative mb-1">
        {item.rank === 1 && (
          <Crown className="absolute -top-4 left-1/2 size-5 -translate-x-1/2 text-chart-3" />
        )}
        <UserAvatar
          displayName={item.displayName}
          className={cn("size-12", isMe && "ring-2 ring-primary ring-offset-2")}
        />
      </div>
      <span
        className={cn(
          "mb-1 w-full truncate text-center text-sm font-semibold",
          isMe && "text-primary"
        )}
        title={item.displayName}
      >
        {item.displayName}
      </span>
      <div
        className={cn(
          "flex w-full flex-col items-center justify-start rounded-t-xl pt-2",
          style.bar,
          style.height
        )}
      >
        <span className="text-lg font-black tabular-nums">#{item.rank}</span>
        <span className="text-xs font-medium tabular-nums opacity-80">
          {item.primary}
        </span>
      </div>
    </div>
  )
}

const ListRow = ({
  item,
  isMe,
  index,
}: {
  item: ScoreItem
  isMe: boolean
  index: number
}) => (
  <li
    className={cn(
      "grid grid-cols-[2rem_auto_minmax(0,1fr)_auto] items-center gap-3 rounded-xl bg-card px-3 py-2 ring-1 ring-foreground/10",
      motion,
      "motion-safe:slide-in-from-bottom-2",
      isMe && "ring-2 ring-primary/70"
    )}
    style={{ animationDelay: `${900 + index * 60}ms` }}
    aria-current={isMe ? "true" : undefined}
  >
    <span className="text-center text-sm font-semibold text-muted-foreground tabular-nums">
      {item.rank}
    </span>
    <UserAvatar displayName={item.displayName} />
    <div className="min-w-0">
      <p
        className={cn(
          "truncate text-sm font-semibold",
          isMe && "text-primary"
        )}
        title={item.displayName}
      >
        {item.displayName}
        {isMe && <span className="ml-1.5 text-xs font-normal">(you)</span>}
      </p>
      {item.secondary && (
        <p className="truncate text-xs text-muted-foreground tabular-nums">
          {item.secondary}
        </p>
      )}
    </div>
    <span
      className={cn(
        "text-sm font-bold tabular-nums",
        item.muted && "font-medium text-muted-foreground/50"
      )}
    >
      {item.primary}
    </span>
  </li>
)

// Podium for the top three, a short list below it, and the current player
// pinned at the bottom if they are outside what is shown. Items are rendered in
// the order received and ranks are shown as sent.
export const ScoreView = ({ items }: { items?: ScoreItem[] }) => {
  const session = useGameSession()
  if (!items || items.length === 0) return null

  const myId = session?.role === "player" ? session.playerId : null
  const podium = items.slice(0, PODIUM_SIZE)
  const list = items.slice(PODIUM_SIZE, PODIUM_SIZE + MAX_LIST_ROWS)
  const mineIndex = myId ? items.findIndex((i) => i.playerId === myId) : -1
  const pinned =
    mineIndex >= PODIUM_SIZE + MAX_LIST_ROWS ? items[mineIndex] : null

  // 2nd, 1st, 3rd left to right
  const podiumOrder = [1, 0, 2].filter((i) => i < podium.length)

  return (
    <div className="flex w-full min-w-0 flex-col gap-6">
      <div className="flex items-end justify-center gap-2">
        {podiumOrder.map((i) => (
          <PodiumStep
            key={podium[i].playerId}
            item={podium[i]}
            position={i}
            isMe={podium[i].playerId === myId}
          />
        ))}
      </div>

      {(list.length > 0 || pinned) && (
        <ul className="mx-auto flex w-full max-w-xl flex-col gap-2">
          {list.map((item, index) => (
            <ListRow
              key={item.playerId}
              item={item}
              isMe={item.playerId === myId}
              index={index}
            />
          ))}
          {pinned && (
            <>
              <li
                aria-hidden
                className="text-center text-xs tracking-widest text-muted-foreground"
              >
                ...
              </li>
              <ListRow item={pinned} isMe index={list.length} />
            </>
          )}
        </ul>
      )}
    </div>
  )
}
