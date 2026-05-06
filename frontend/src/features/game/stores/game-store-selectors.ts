import { useShallow } from "zustand/react/shallow"
import { useGameStore } from "@/features/game/stores/game-store"

export const useGameSession = () => useGameStore(useShallow((s) => s.session))

export const useGameState = () => useGameStore((s) => s.state)
export const useGameSetState = () => useGameStore((s) => s.setState)

export const useGamePlayers = () => useGameStore(useShallow((s) => s.players))

export const useGameSetHost = () => useGameStore((s) => s.setHost)
export const useGameSetPlayer = () => useGameStore((s) => s.setPlayer)
export const useGameClearSession = () => useGameStore((s) => s.clearSession)

export const useGameSetPlayers = () => useGameStore((s) => s.setPlayers)
export const useGameAddPlayer = () => useGameStore((s) => s.addPlayer)

export const useGameRemovePlayer = () => useGameStore((s) => s.removePlayer)

export const useGameReset = () => useGameStore((s) => s.reset)
