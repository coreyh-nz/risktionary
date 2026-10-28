import * as yup from "yup"
import { RoundPhaseType } from "../../types/round/phase/round"

export type ConfigurableTimerPhase = Exclude<RoundPhaseType, "COMPLETED" | "SAVING">
export type FeedbackType = "NONE" | "AI"
export type FeedbackAllocationMode = "BALANCED_RANDOM" | "FIXED"
export type FeedbackTiming = "DELAYED" | "INSTANT"
export type FeedbackFraming = "POSITIVE" | "CORRECTIVE" | "NEUTRAL"

export type TimerValue = number | "none"

export interface DurationOption {
  label: string
  value: TimerValue
}

export interface ConfigurableTimer {
  key: ConfigurableTimerPhase
  label: string
  description: string
}

export interface FeedbackOption {
  value: FeedbackType
  label: string
  description: string
}

export interface FeedbackAllocationModeOption {
  value: FeedbackAllocationMode
  label: string
  description: string
}

export interface FeedbackTimingOption {
  value: FeedbackTiming
  label: string
  description: string
}

export interface FeedbackFramingOption {
  value: FeedbackFraming
  label: string
  description: string
}

const TIMERS = {
  FIVE_SECONDS: 5000,
  TEN_SECONDS: 10000,
  FIFTEEN_SECONDS: 15000,
  TWENTY_SECONDS: 20000,
  THIRTY_SECONDS: 30000,
  FORTY_FIVE_SECONDS: 45000,
  SIXTY_SECONDS: 60000,
  NINETY_SECONDS: 90000,
  TWO_MINUTES: 120000,
  THREE_MINUTES: 180000,
  FIVE_MINUTES: 300000,
  NONE: "none",
} as const

export const lobbyCountdownOptions: DurationOption[] = [
  { label: "5 sec", value: TIMERS.FIVE_SECONDS },
  { label: "10 sec", value: TIMERS.TEN_SECONDS },
  { label: "20 sec", value: TIMERS.TWENTY_SECONDS },
  { label: "15 sec", value: TIMERS.FIFTEEN_SECONDS },
  { label: "30 sec", value: TIMERS.THIRTY_SECONDS },
  { label: "45 sec", value: TIMERS.FORTY_FIVE_SECONDS },
  { label: "60 sec", value: TIMERS.SIXTY_SECONDS },
  { label: "90 sec", value: TIMERS.NINETY_SECONDS },
  { label: "2 min", value: TIMERS.TWO_MINUTES },
  { label: "3 min", value: TIMERS.THREE_MINUTES },
  { label: "5 min", value: TIMERS.FIVE_MINUTES },
]

export const phaseTimerOptions: DurationOption[] = [
  ...lobbyCountdownOptions,
  { label: "No timer", value: TIMERS.NONE },
]

type PhaseTimerValue = (typeof phaseTimerOptions)[number]["value"]

export const phaseTimers: ConfigurableTimer[] = [
  {
    key: "DRAWING",
    label: "Drawing",
    description: "Players draw the selected risk",
  },
  {
    key: "DRAWING_REVIEW",
    label: "Drawing Review",
    description: "Review the drawing and guesses",
  },
  {
    key: "RANKING",
    label: "Ranking",
    description: "Players rank likelihood and consequence",
  },
  {
    key: "RANKING_REVIEW",
    label: "Ranking Review",
    description: "Review the room's risk assessment",
  },
  {
    key: "WORD_REVIEW",
    label: "Word Review",
    description: "Reveal and discuss the risk word",
  },
  {
    key: "SCORING",
    label: "Scoring",
    description: "Award points and show feedback",
  },
] as const

export const feedbackOptions: FeedbackOption[] = [
  { value: "NONE", label: "None", description: "Keep the game feedback-free." },
  {
    value: "AI",
    label: "AI Generated",
    description: "Personalised feedback from the game.",
  },
]

export const aiSnapshotIntervalOptions: DurationOption[] = [
  { label: "5 sec", value: TIMERS.FIVE_SECONDS },
  { label: "10 sec", value: TIMERS.TEN_SECONDS },
  { label: "15 sec", value: TIMERS.FIFTEEN_SECONDS },
  { label: "20 sec", value: TIMERS.TWENTY_SECONDS },
  { label: "30 sec", value: TIMERS.THIRTY_SECONDS },
]

export const feedbackAllocationModeOptions: FeedbackAllocationModeOption[] = [
  {
    value: "BALANCED_RANDOM",
    label: "Balanced random allocation",
    description:
      "Randomly assign timing and framing per player, balanced across conditions.",
  },
  {
    value: "FIXED",
    label: "Fixed for all players",
    description: "Every player receives the same timing and framing.",
  },
]

export const feedbackTimingOptions: FeedbackTimingOption[] = [
  {
    value: "DELAYED",
    label: "Delayed",
    description: "Feedback is shown after the round ends.",
  },
  {
    value: "INSTANT",
    label: "Instant",
    description: "Feedback is shown as soon as it's generated.",
  },
]

export const feedbackFramingOptions: FeedbackFramingOption[] = [
  {
    value: "POSITIVE",
    label: "Positive",
    description: "Feedback emphasises what went well.",
  },
  {
    value: "CORRECTIVE",
    label: "Corrective",
    description: "Feedback focuses on what to improve.",
  },
  {
    value: "NEUTRAL",
    label: "Neutral",
    description: "Feedback is factual, without a positive or corrective tone.",
  },
]

export const timerSchema = yup.object(
  Object.fromEntries(
    phaseTimers.map((t) => [
      t.key,
      yup
        .mixed<PhaseTimerValue>()
        .required()
        .oneOf(phaseTimerOptions.map((o) => o.value)),
    ])
  ) as Record<ConfigurableTimerPhase, yup.Schema<PhaseTimerValue>>
)

export const createGameSchema = yup.object({
  wordIds: yup.array(yup.string().required()).min(1).required(),
  lobbyCountdown: yup.number().required(),
  skippingCountdownsEnabled: yup.boolean().required(),
  timers: timerSchema,
  feedbackType: yup.mixed<FeedbackType>().oneOf(["NONE", "AI"]).required(),
  aiSnapshotIntervalMs: yup
    .number()
    .when("feedbackType", ([feedbackType], schema) =>
      feedbackType === "AI" ? schema.required() : schema.optional()
    ),
  feedbackAllocationMode: yup
    .mixed<FeedbackAllocationMode>()
    .oneOf(["BALANCED_RANDOM", "FIXED"])
    .when("feedbackType", ([feedbackType], schema) =>
      feedbackType === "AI" ? schema.required() : schema.optional()
    ),
  feedbackTiming: yup
    .mixed<FeedbackTiming>()
    .oneOf(["DELAYED", "INSTANT"])
    .when(
      ["feedbackType", "feedbackAllocationMode"],
      ([feedbackType, feedbackAllocationMode], schema) =>
        feedbackType === "AI" && feedbackAllocationMode === "FIXED"
          ? schema.required()
          : schema.optional()
    ),
  feedbackFraming: yup
    .mixed<FeedbackFraming>()
    .oneOf(["POSITIVE", "CORRECTIVE", "NEUTRAL"])
    .when(
      ["feedbackType", "feedbackAllocationMode"],
      ([feedbackType, feedbackAllocationMode], schema) =>
        feedbackType === "AI" && feedbackAllocationMode === "FIXED"
          ? schema.required()
          : schema.optional()
    ),
})

export type CreateGameFormValues = yup.InferType<typeof createGameSchema>

export const createGameDefaultValues: CreateGameFormValues = {
  wordIds: [],
  lobbyCountdown: TIMERS.THIRTY_SECONDS,
  skippingCountdownsEnabled: true,
  timers: {
    DRAWING: TIMERS.NINETY_SECONDS,
    DRAWING_REVIEW: TIMERS.TEN_SECONDS,
    RANKING: TIMERS.SIXTY_SECONDS,
    RANKING_REVIEW: TIMERS.THIRTY_SECONDS,
    WORD_REVIEW: TIMERS.THIRTY_SECONDS,
    SCORING: TIMERS.THIRTY_SECONDS,
  },
  feedbackType: "NONE",
  aiSnapshotIntervalMs: TIMERS.TEN_SECONDS,
  feedbackAllocationMode: "BALANCED_RANDOM",
  feedbackTiming: "DELAYED",
  feedbackFraming: "NEUTRAL",
}
