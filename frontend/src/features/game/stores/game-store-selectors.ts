import { useGameStore } from "@/features/game/stores/game-store"
import { useShallow } from "zustand/react/shallow"

export const useGameSession = () => useGameStore(useShallow((s) => s.session))

export const useGameState = () => useGameStore((s) => s.state)
export const useGameSetState = () => useGameStore((s) => s.setState)

export const useGamePlayers = () => useGameStore(useShallow((s) => s.players))

export const useGameSetHost = () => useGameStore((s) => s.setHost)
export const useGameSetPlayer = () => useGameStore((s) => s.setPlayer)
export const useGameClearSession = () => useGameStore((s) => s.clearSession)
export const useGameIsHost = () =>
  useGameStore((s) => s.session?.role === "host")
export const useGameIsPlayer = () =>
  useGameStore((s) => s.session?.role === "player")
export const useGameIsPlayerMe = (id: string) =>
  useGameStore((s) => {
    const session = s.session
    return session?.role === "player" && session?.playerId === id
  })

export const useGameSetPlayers = () => useGameStore((s) => s.setPlayers)
export const useGameAddPlayer = () => useGameStore((s) => s.addPlayer)

export const useGameRemovePlayer = () => useGameStore((s) => s.removePlayer)

export const useGameReset = () => useGameStore((s) => s.reset)

export const useGameIsDrawer = () =>
  useGameStore((s) => {
    const session = s.session
    const round = s.round
    if (!session || !round) throw new Error()

    return (
      session.role === "player" &&
      round.state.type === "IN_PROGRESS" &&
      session.playerId === round.state.drawer.id
    )
  })

export const useVolunteers = () => useGameStore(useShallow((s) => s.volunteers))
export const useSetVolunteers = () => useGameStore((s) => s.setVolunteers)

export const useGameRoundRole = () =>
  useGameStore(useShallow((s) => s.roundRole))

export const useGameRoundChatMessages = () =>
  useGameStore(useShallow((s) => s.roundChatMessages))

export const useGameFeedbackEnabled = () =>
  useGameStore((s) => s.feedbackEnabled)

export const useGameRoundCorrectGuessesCount = () =>
  useGameStore((s) => s.roundCorrectGuessesCount)
