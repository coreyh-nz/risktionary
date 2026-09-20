import { Button } from "@/components/ui/button"
import {
  BookOpen,
  CheckCheck,
  Eye,
  ListOrdered,
  LucideIcon,
  Pencil,
  Save,
  SkipForward,
  Trophy,
} from "lucide-react"
import { RoundNotActiveError, RoundNotInProgress } from "../../errors/round"
import { useCountdown } from "../../hooks/shared/countdown/use-countdown"
import { useGameIsHost } from "../../stores/game-store-selectors"
import { useGameRound } from "../../stores/selectors/round.selectors"
import { RoundPhase, RoundPhaseType } from "../../types/round/phase/round"
import { CountdownDigit } from "../shared/countdown/countdown-digit"
import { CountdownRing } from "../shared/countdown/countdown-ring"

const ROUND_PHASE_META: Record<
  RoundPhaseType,
  { label: string; icon: LucideIcon }
> = {
  DRAWING: { label: "Drawing", icon: Pencil },
  DRAWING_REVIEW: { label: "Drawing Review", icon: Eye },
  RANKING: { label: "Ranking", icon: ListOrdered },
  RANKING_REVIEW: { label: "Ranking Review", icon: CheckCheck },
  WORD_REVIEW: { label: "Word Review", icon: BookOpen },
  SAVING: { label: "Saving", icon: Save },
  SCORING: { label: "Scoring", icon: Trophy },
  COMPLETED: { label: "Completed", icon: Trophy },
}

export const RoundPhaseStatusBar = () => {
  const round = useGameRound()
  const isHost = useGameIsHost()

  if (!round) throw new RoundNotActiveError()
  if (round.state.type !== "IN_PROGRESS") throw new RoundNotInProgress()

  const phase = round.state.phase
  const { label, icon: Icon } = ROUND_PHASE_META[phase.type]

  const onSkip = () => {}

  return (
    <div className="w-full shrink-0 border-b border-primary/15 bg-card">
      <div className="mx-auto flex w-full max-w-6xl items-center justify-between gap-3 px-4 py-2.5">
        {/* countdown */}
        <RoundStatusBarCountdown phase={phase} />

        {/* current phase */}
        <div className="flex items-center gap-2 rounded-full border border-primary/20 bg-primary/10 px-4 py-1.5">
          <Icon className="size-4 text-primary" />
          <span className="text-sm font-bold tracking-tight text-foreground">
            {label}
          </span>
        </div>

        {/* round + host skip */}
        <div className="flex min-w-20 items-center justify-end gap-3">
          <div className="flex flex-col items-end leading-none">
            <span className="text-[10px] font-semibold tracking-wide text-muted-foreground uppercase">
              Round
            </span>
            <span className="text-base font-black tabular-nums text-foreground">
              {round.number}
            </span>
          </div>
          {isHost && (
            <Button size="sm" variant="secondary" onClick={onSkip}>
              Skip
              <SkipForward />
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}

export const RoundStatusBarCountdown = ({ phase }: { phase: RoundPhase }) => {
  const countdown = useCountdown(phase.countdown)
  if (countdown.secondsLeft == undefined || countdown.totalSeconds == undefined)
    return null

  return (
    <div className="flex min-w-20 items-center gap-2">
      <div className="relative flex shrink-0 items-center justify-center">
        <CountdownRing
          secondsLeft={countdown.secondsLeft}
          total={countdown.totalSeconds}
          size="sm"
        />

        <CountdownDigit
          secondsLeft={countdown.secondsLeft}
          size="sm"
          className="absolute"
        />
      </div>

      <span className="hidden text-xs font-medium tracking-wide text-muted-foreground uppercase sm:inline">
        left
      </span>
    </div>
  )
}
