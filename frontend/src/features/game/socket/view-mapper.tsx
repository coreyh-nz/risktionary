import { assertNever } from "@/lib/utils"
import { mapTimerViewToCountdown } from "@/mappers/time"
import { GameState } from "../types/game"
import { GameStateView } from "../types/game/events"
import { RoundPhaseStateView, RoundStateView, RoundView, } from "../types/round/phase/events"
import { Round, RoundPhase, RoundState } from "../types/round/phase/round"

export const mapGameStateViewToGameState = (
  stateView: GameStateView
): GameState => {
  const type = stateView.type
  switch (type) {
    case "STARTING":
      return {
        type: "STARTING",
        countdown: mapTimerViewToCountdown(stateView.timer),
      }
    case "LOBBY":
    case "IN_PROGRESS":
      return { type: stateView.type }
    case "COMPLETED":
      return { type: "COMPLETED", standings: stateView.standings }
    default:
      assertNever(type)
  }
}

export const mapRoundViewToRound = (roundView: RoundView): Round => {
  return {
    ...roundView,
    state: mapRoundStateViewToRoundState(roundView.state),
  }
}

export const mapRoundStateViewToRoundState = (
  stateView: RoundStateView
): RoundState => {
  // phases carry timers that need converting to countdowns, including when
  // the state comes from a resync
  if (stateView.type === "IN_PROGRESS") {
    return {
      ...stateView,
      phase: mapRoundPhaseStateViewToRoundPhaseState(stateView.phase),
    }
  }
  return stateView
}

export const mapRoundPhaseStateViewToRoundPhaseState = (
  stateView: RoundPhaseStateView
): RoundPhase => {
  return {
    ...stateView,
    countdown: stateView.timer
      ? mapTimerViewToCountdown(stateView.timer)
      : undefined,
  }
}
