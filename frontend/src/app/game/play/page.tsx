"use client"

import { GamePlayLobbyScreen } from "@/features/lobby/components/game-lobby-screen"
import { useGameStore } from "@/stores/game-store"
import { assertNever } from "@/lib/utils"

const GamePlayPage = () => {
  const { state, session } = useGameStore()

  if (!session) {
    return <div>no session</div>
  }

  switch (state) {
    case "INITIALIZING":
      return <p>INITIALIZING</p>
    case "LOBBY":
      return <GamePlayLobbyScreen />
    case "STARTING":
      return <p>STARTING</p>
    case "IN_PROGRESS":
      return <p>IN_PROGRESS</p>
    case "PAUSED":
      return <p>PAUSED</p>
    case "COMPLETED":
      return <p>COMPLETED</p>
    default:
      // will give a compiler error if there is a
      // unhandled state in switch case
      assertNever(state)
  }
}

export default GamePlayPage
