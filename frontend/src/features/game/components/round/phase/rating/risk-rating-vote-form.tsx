import { Stack } from "@/components/layout/stack"
import { LevelSlider } from "@/components/level-slider"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
  FieldSet,
} from "@/components/ui/field"
import { useRiskRatingCommands } from "@/features/game/hooks/round/phase/risk/use-risk-rating-commands"
import { useGameRoundSetSubmittedRiskRating } from "@/features/game/stores/selectors/round.selectors"
import {
  RISK_LIKELIHOOD,
  RISK_LIKELIHOODS,
  RISK_SEVERITIES,
  RISK_SEVERITY,
  RiskLikelihood,
  RiskSeverity,
} from "@/features/game/types/round/phase/risk/risk"
import { SubmitEvent, useState } from "react"

export const RiskRatingVoteForm = () => {
  const [likelihood, setLikelihood] = useState<RiskLikelihood>(
    RISK_LIKELIHOOD.POSSIBLE.id
  )
  const [severity, setSeverity] = useState<RiskSeverity>(
    RISK_SEVERITY.MODERATE.id
  )
  const [submitting, setSubmitting] = useState<boolean>(false)

  const { onRiskRating } = useRiskRatingCommands()
  const setRoundSubmittedRiskRating = useGameRoundSetSubmittedRiskRating()

  const onSubmit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    setSubmitting(true)
    try {
      onRiskRating(likelihood, severity)
      setRoundSubmittedRiskRating({ likelihood, severity })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <Stack>
        <Card>
          <CardContent>
            <FieldSet>
              <FieldGroup className="gap-8">
                <Field>
                  <FieldLabel htmlFor="likelihood-slider">
                    How likely is this risk to occur?
                  </FieldLabel>
                  <FieldDescription>
                    Consider how often this could happen over the life of the
                    project.
                  </FieldDescription>
                  <LevelSlider
                    levels={RISK_LIKELIHOODS}
                    value={likelihood}
                    onValueChange={setLikelihood}
                  />
                </Field>

                <Field>
                  <FieldLabel htmlFor="severity-slider">
                    How severe would the impact be?
                  </FieldLabel>
                  <FieldDescription>
                    Consider the worst realistic outcome if this risk occurs.
                  </FieldDescription>
                  <LevelSlider
                    levels={RISK_SEVERITIES}
                    value={severity}
                    onValueChange={setSeverity}
                  />
                </Field>
              </FieldGroup>
            </FieldSet>
          </CardContent>
        </Card>

        <Button type="submit" disabled={submitting}>
          Lock in vote
        </Button>
      </Stack>
    </form>
  )
}
