"use client"

import { GameScreen } from "@/features/game/components/game-screen"
import { WebSocketProvider } from "@/providers/web-socket-provider"

const GamePlayPage = () => {
  return (
    <WebSocketProvider>
      <GameScreen />
    </WebSocketProvider>
  )
}

export default GamePlayPage
