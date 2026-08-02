import { useShallow } from "zustand/shallow"
import { useGameStore } from "../game-store"

export const useGameRound = () => useGameStore(useShallow((s) => s.round))

export const useGameSetRound = () => useGameStore((s) => s.setRound)
