import { GameState } from "../types/game"
import { GameStateView } from "../types/game/events"
import {
  RoundPhaseStateView,
  RoundStateView,
} from "../types/round/phase/events"
import { RoundPhase, RoundState } from "../types/round/phase/round"

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
