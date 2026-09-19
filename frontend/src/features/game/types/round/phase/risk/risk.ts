export interface RiskLevel<T extends string> {
  id: T
  label: string
  value: number
}

export const RISK_LIKELIHOOD = {
  RARE: {
    id: "RARE",
    label: "Rare",
    value: 1,
  },
  UNLIKELY: {
    id: "UNLIKELY",
    label: "Unlikely",
    value: 2,
  },
  POSSIBLE: {
    id: "POSSIBLE",
    label: "Possible",
    value: 3,
  },
  LIKELY: {
    id: "LIKELY",
    label: "Likely",
    value: 4,
  },
  ALMOST_CERTAIN: {
    id: "ALMOST_CERTAIN",
    label: "Almost Certain",
    value: 5,
  },
} as const satisfies Record<string, RiskLevel<string>>

export const RISK_LIKELIHOODS = Object.values(
  RISK_LIKELIHOOD
) as readonly RiskLevel<RiskLikelihood>[]
export type RiskLikelihood = keyof typeof RISK_LIKELIHOOD

export const RISK_SEVERITY = {
  INSIGNIFICANT: {
    id: "INSIGNIFICANT",
    label: "Insignificant",
    value: 1,
  },
  MINOR: {
    id: "MINOR",
    label: "Minor",
    value: 2,
  },
  MODERATE: {
    id: "MODERATE",
    label: "Moderate",
    value: 3,
  },
  MAJOR: {
    id: "MAJOR",
    label: "Major",
    value: 4,
  },
  CATASTROPHIC: {
    id: "CATASTROPHIC",
    label: "Catastrophic",
    value: 5,
  },
} as const satisfies Record<string, RiskLevel<string>>

export const RISK_SEVERITIES = Object.values(
  RISK_SEVERITY
) as readonly RiskLevel<RiskSeverity>[]

export type RiskSeverity = keyof typeof RISK_SEVERITY

export interface RiskRating {
  likelihood: RiskLikelihood
  severity: RiskSeverity
}

export interface RiskRatingCount extends RiskRating {
  count: number
}
