"use client"

import { Card, CardContent } from "@/components/ui/card"
import {
  RISK_LIKELIHOODS,
  RISK_SEVERITIES,
  RiskLikelihood,
  RiskSeverity,
} from "@/features/game/types/round/phase/risk/risk"
import { ReactNode } from "react"

interface RiskMatrixProps {
  renderCell: (likelihood: RiskLikelihood, severity: RiskSeverity) => ReactNode
}

export const RiskMatrix = ({ renderCell }: RiskMatrixProps) => {
  return (
    <Card>
      <CardContent>
        <div className="flex w-full flex-col gap-2">
          <div className="flex gap-2">
            <div className="flex items-center justify-center">
              <span className="text-xs font-bold tracking-widest text-muted-foreground uppercase [writing-mode:vertical-rl] rotate-180">
                Severity
              </span>
            </div>

            <div className="flex flex-1 flex-col gap-2">
              {RISK_SEVERITIES.map((severity) => (
                <div key={severity.id} className="flex items-stretch gap-2">
                  <span className="flex w-20 shrink-0 items-center justify-end text-right text-[11px] font-semibold leading-tight text-muted-foreground">
                    {severity.label}
                  </span>

                  <div
                    className="grid flex-1 gap-2"
                    style={{
                      gridTemplateColumns: `repeat(${RISK_LIKELIHOODS.length}, minmax(0, 1fr))`,
                    }}
                  >
                    {RISK_LIKELIHOODS.map((likelihood) =>
                      renderCell(likelihood.id, severity.id)
                    )}
                  </div>
                </div>
              ))}

              <div className="flex gap-2">
                <span className="w-20 shrink-0" />
                <div
                  className="grid flex-1 gap-2"
                  style={{
                    gridTemplateColumns: `repeat(${RISK_LIKELIHOODS.length}, minmax(0, 1fr))`,
                  }}
                >
                  {RISK_LIKELIHOODS.map((likelihood) => (
                    <span
                      key={likelihood.id}
                      className="text-center text-[11px] font-semibold leading-tight text-muted-foreground"
                    >
                      {likelihood.label}
                    </span>
                  ))}
                </div>
              </div>
              <div className="flex">
                <span className="w-20 shrink-0" />
                <span className="flex-1 text-center text-xs font-bold tracking-widest text-muted-foreground uppercase">
                  Likelihood
                </span>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
