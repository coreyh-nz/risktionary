import { GameCompletedScreen } from "@/features/game/components/game-completed-screen"
import { GameInitialisingScreen } from "@/features/game/components/game-initialising-screen"
import { GamePlayLobbyScreen } from "@/features/game/components/lobby/game-lobby-screen"
import { CurrentRound } from "@/features/game/components/round/current-round"
import { GameStateType } from "@/features/game/types/game"
import { ComponentType } from "react"

export const GAME_STATE_COMPONENTS: Record<GameStateType, ComponentType> = {
  INITIALIZING: GameInitialisingScreen,
  LOBBY: GamePlayLobbyScreen,
  STARTING: GamePlayLobbyScreen,
  IN_PROGRESS: CurrentRound,
  COMPLETED: GameCompletedScreen,
}
