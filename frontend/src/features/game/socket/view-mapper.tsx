import { GameState } from "../types/game"
import { GameStateView } from "../types/game/events"
import { RoundPhase, RoundState } from "../types/round"
import { RoundPhaseStateView, RoundStateView } from "../types/round/events"

export const mapGameStateViewToGameState = (
  stateView: GameStateView
): GameState => {
  return stateView
}

export const mapRoundStateViewToRoundState = (
  stateView: RoundStateView
): RoundState => {
  return stateView
}

export const mapRoundPhaseStateViewToRoundPhaseState = (
  stateView: RoundPhaseStateView
): RoundPhase => {
  return stateView
}
