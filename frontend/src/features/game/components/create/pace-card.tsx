import { StepIndicator } from "@/components/common/step-indicator"
import { FormSelect } from "@/components/form"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { SelectItem } from "@/components/ui/select"
import type { Control } from "react-hook-form"
import {
  CreateGameFormValues,
  lobbyCountdownOptions,
  phaseTimerOptions,
  phaseTimers,
} from "../../lib/schemas/create-game-schema"

interface PaceCardProps {
  control: Control<CreateGameFormValues>
}

export const PaceCard = ({ control }: PaceCardProps) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <StepIndicator>2</StepIndicator>
          Set the pace
        </CardTitle>
        <CardDescription>
          Each phase advances automatically when its timer ends.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Card className="py-0!">
          <div className="divide-y divide-border">
            <FormSelect
              control={control}
              name="lobbyCountdown"
              className="p-3"
              label="Lobby countdown"
              description="How long players wait in the lobby before the game starts."
              items={lobbyCountdownOptions}
            >
              {lobbyCountdownOptions.map((item) => (
                <SelectItem key={item.value} value={item.value}>
                  {item.label}
                </SelectItem>
              ))}
            </FormSelect>

            {phaseTimers.map((timer) => (
              <FormSelect
                key={timer.key}
                control={control}
                name={`timers.${timer.key}`}
                className="p-3"
                label={timer.label}
                description={timer.description}
                items={phaseTimerOptions}
              >
                {phaseTimerOptions.map((item) => (
                  <SelectItem key={item.value} value={item.value}>
                    {item.label}
                  </SelectItem>
                ))}
              </FormSelect>
            ))}
          </div>
        </Card>
      </CardContent>
    </Card>
  )
}
