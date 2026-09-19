import { FormSubmitButton } from "@/components/form"
import { Stack } from "@/components/layout/stack"
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Clock, Hourglass, Play, Sparkles } from "lucide-react"
import type { Control } from "react-hook-form"
import { useWatch } from "react-hook-form"
import {
  aiSnapshotIntervalOptions,
  CreateGameFormValues,
  feedbackOptions,
  lobbyCountdownOptions,
  phaseTimers,
} from "../../lib/schemas/create-game-schema"

interface SummaryCardProps {
  control: Control<CreateGameFormValues>
}

const formatDuration = (ms: number) => {
  if (ms <= 0) return "0 sec"
  if (ms < 60000) return `${Math.round(ms / 1000)} sec`
  const minutes = ms / 60000
  return `${minutes % 1 === 0 ? minutes : minutes.toFixed(1)} min`
}

export const SummaryCard = ({ control }: SummaryCardProps) => {
  const wordIds = useWatch({ control, name: "wordIds" }) ?? []
  const lobbyCountdown = useWatch({ control, name: "lobbyCountdown" })
  const feedbackType = useWatch({ control, name: "feedbackType" })
  const aiSnapshotIntervalMs = useWatch({
    control,
    name: "aiSnapshotIntervalMs",
  })
  const timers = useWatch({ control, name: "timers" })

  const rounds = wordIds.length
  const feedbackLabel = feedbackOptions.find(
    (o) => o.value === feedbackType
  )?.label
  const lobbyLabel = lobbyCountdownOptions.find(
    (o) => o.value === lobbyCountdown
  )?.label
  const snapshotLabel = aiSnapshotIntervalOptions.find(
    (o) => o.value === aiSnapshotIntervalMs
  )?.label

  const perRoundMs = phaseTimers.reduce((total, timer) => {
    const value = timers?.[timer.key]
    return total + (typeof value === "number" ? value : 0)
  }, 0)
  const estimatedTotalMs =
    (typeof lobbyCountdown === "number" ? lobbyCountdown : 0) +
    rounds * perRoundMs

  return (
    <Card className="overflow-hidden pt-0">
      <CardHeader className="bg-primary text-primary-foreground py-4">
        <span className="uppercase text-xs font-medium text-primary-foreground/80">
          Ready to play?
        </span>
        <CardTitle className="font-semibold">
          {rounds} {rounds === 1 ? "round" : "rounds"} configured
        </CardTitle>
      </CardHeader>
      <CardContent>
        <Stack className="gap-3">
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Lobby countdown</span>
            <span className="font-medium">{lobbyLabel ?? "—"}</span>
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Feedback</span>
            <span className="font-medium">{feedbackLabel ?? "—"}</span>
          </div>
          {feedbackType === "AI" && (
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Snapshot interval</span>
              <span className="font-medium">{snapshotLabel ?? "—"}</span>
            </div>
          )}

          <Separator />

          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Hourglass className="size-4 shrink-0" />
            <span>
              ~{formatDuration(estimatedTotalMs)} estimated game length
            </span>
          </div>
          {feedbackType === "AI" && (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Sparkles className="size-4 shrink-0" />
              <span>AI feedback is generated live during each round</span>
            </div>
          )}
          {rounds === 0 && (
            <div className="flex items-center gap-2 text-sm text-destructive">
              <Clock className="size-4 shrink-0" />
              <span>Add at least one word to start</span>
            </div>
          )}
        </Stack>
      </CardContent>
      <CardFooter>
        <FormSubmitButton className="w-full" disabled={rounds === 0}>
          <Play />
          Start game
        </FormSubmitButton>
      </CardFooter>
    </Card>
  )
}
