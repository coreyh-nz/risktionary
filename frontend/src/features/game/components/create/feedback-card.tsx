import { StepIndicator } from "@/components/common/step-indicator"
import { FormCustom, FormSelect } from "@/components/form"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { SelectableCard } from "@/components/ui/card-variants"
import { Field, FieldDescription, FieldLabel } from "@/components/ui/field"
import { SelectItem } from "@/components/ui/select"
import { Check } from "lucide-react"
import { useEffect, useRef } from "react"
import type { Control } from "react-hook-form"
import { useWatch } from "react-hook-form"
import {
  aiSnapshotIntervalOptions,
  CreateGameFormValues,
  feedbackAllocationModeOptions,
  feedbackFramingOptions,
  feedbackOptions,
  feedbackTimingOptions,
} from "../../lib/schemas/create-game-schema"

interface FeedbackCardProps {
  control: Control<CreateGameFormValues>
}

export const FeedbackCard = ({ control }: FeedbackCardProps) => {
  const feedbackType = useWatch({ control, name: "feedbackType" })
  const feedbackAllocationMode = useWatch({
    control,
    name: "feedbackAllocationMode",
  })

  const aiOptionsRef = useRef<HTMLDivElement>(null)
  const fixedOptionsRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (feedbackType === "AI") {
      aiOptionsRef.current?.scrollIntoView({
        behavior: "smooth",
        block: "end",
      })
    }
  }, [feedbackType])

  useEffect(() => {
    if (feedbackType === "AI" && feedbackAllocationMode === "FIXED") {
      fixedOptionsRef.current?.scrollIntoView({
        behavior: "smooth",
        block: "end",
      })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [feedbackAllocationMode])

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <StepIndicator>3</StepIndicator>
          Player feedback
        </CardTitle>
        <CardDescription>
          Choose how feedback is generated and when players receive it.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <FormCustom control={control} name="feedbackType">
          {({ value, onChange }) => (
            <div className="grid gap-2 grid-cols-[repeat(auto-fit,minmax(10rem,1fr))]">
              {feedbackOptions.map((option) => (
                <SelectableCard
                  key={option.value}
                  size="sm"
                  selected={value === option.value}
                  onSelect={() => onChange(option.value)}
                >
                  <CardContent>
                    <Field>
                      <FieldLabel className="w-full flex justify-between text-sm">
                        {option.label}
                        {value === option.value && (
                          <Check className="shrink-0 size-5" />
                        )}
                      </FieldLabel>
                      <FieldDescription className="text-xs">
                        {option.description}
                      </FieldDescription>
                    </Field>
                  </CardContent>
                </SelectableCard>
              ))}
            </div>
          )}
        </FormCustom>

        {feedbackType === "AI" && (
          <div ref={aiOptionsRef}>
            <Card className="py-0! mt-4">
              <div className="divide-y divide-border">
                <FormSelect
                  control={control}
                  name="aiSnapshotIntervalMs"
                  className="p-3"
                  label="Snapshot interval"
                  description="How often the drawing is captured and sent to the AI for analysis."
                  items={aiSnapshotIntervalOptions}
                >
                  {aiSnapshotIntervalOptions.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </FormSelect>
              </div>
            </Card>

            <Field className="mt-4">
              <FieldLabel>Feedback allocation</FieldLabel>
              <FieldDescription>
                Decide how feedback timing and framing are assigned to
                players.
              </FieldDescription>
            </Field>
            <FormCustom control={control} name="feedbackAllocationMode">
              {({ value, onChange }) => (
                <div className="grid gap-2 grid-cols-[repeat(auto-fit,minmax(10rem,1fr))] mt-2">
                  {feedbackAllocationModeOptions.map((option) => (
                    <SelectableCard
                      key={option.value}
                      size="sm"
                      selected={value === option.value}
                      onSelect={() => onChange(option.value)}
                    >
                      <CardContent>
                        <Field>
                          <FieldLabel className="w-full flex justify-between text-sm">
                            {option.label}
                            {value === option.value && (
                              <Check className="shrink-0 size-5" />
                            )}
                          </FieldLabel>
                          <FieldDescription className="text-xs">
                            {option.description}
                          </FieldDescription>
                        </Field>
                      </CardContent>
                    </SelectableCard>
                  ))}
                </div>
              )}
            </FormCustom>

            {feedbackAllocationMode === "FIXED" && (
              <div ref={fixedOptionsRef}>
                <Card className="py-0! mt-4">
                  <div className="divide-y divide-border">
                    <FormSelect
                      control={control}
                      name="feedbackTiming"
                      className="p-3"
                      label="Feedback timing"
                      description="Applied to every player."
                      items={feedbackTimingOptions}
                    >
                      {feedbackTimingOptions.map((item) => (
                        <SelectItem key={item.value} value={item.value}>
                          {item.label}
                        </SelectItem>
                      ))}
                    </FormSelect>
                    <FormSelect
                      control={control}
                      name="feedbackFraming"
                      className="p-3"
                      label="Feedback framing"
                      description="Applied to every player."
                      items={feedbackFramingOptions}
                    >
                      {feedbackFramingOptions.map((item) => (
                        <SelectItem key={item.value} value={item.value}>
                          {item.label}
                        </SelectItem>
                      ))}
                    </FormSelect>
                  </div>
                </Card>
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
