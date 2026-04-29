import { useShallow } from "zustand/react/shallow"
import { useGameStore } from "@/features/game/stores/game-store"

export const useGameSession = () => useGameStore(useShallow((s) => s.session))

export const useGamePhase = () => useGameStore((s) => s.phase)
export const useGameSetPhase = () => useGameStore((s) => s.setPhase)

export const useGamePlayers = () => useGameStore(useShallow((s) => s.players))

export const useGameSetHost = () => useGameStore((s) => s.setHost)
export const useGameSetPlayer = () => useGameStore((s) => s.setPlayer)
export const useGameClearSession = () => useGameStore((s) => s.clearSession)

export const useGameSetPlayers = () => useGameStore((s) => s.setPlayers)
export const useGameAddPlayer = () => useGameStore((s) => s.addPlayer)

export const useGameRemovePlayer = () => useGameStore((s) => s.removePlayer)

export const useGameReset = () => useGameStore((s) => s.reset)
