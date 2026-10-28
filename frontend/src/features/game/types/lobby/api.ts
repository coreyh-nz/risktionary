import type {
  FeedbackAllocationMode,
  FeedbackFraming,
  FeedbackTiming,
  FeedbackType,
} from "../../lib/schemas/create-game-schema"
import { GameSessionHostView } from "../game"
import { RoundPhaseType } from "../round/phase/round"

export interface FeedbackAllocationDto {
  mode: FeedbackAllocationMode
  timing?: FeedbackTiming
  framing?: FeedbackFraming
}

export interface FeedbackConfigurationDto {
  type: FeedbackType
  snapshotIntervalMs?: number
  allocation?: FeedbackAllocationDto
}

export interface GameConfigurationDto {
  wordIds: string[]
  lobbyCountdownMs: number
  phaseDurationsMs: Partial<Record<RoundPhaseType, number>>
  skippingCountdownsEnabled: boolean
  feedback: FeedbackConfigurationDto
}

export interface CreateGameRequest {
  configuration: GameConfigurationDto
}

export interface CreateGameResponse {
  session: GameSessionHostView
}
