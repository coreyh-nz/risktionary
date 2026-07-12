"use client"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  useGameSession,
  useGameState,
} from "@/features/game/stores/game-store-selectors"
import { useGameSocket } from "../../hooks/use-game-socket"
import { PlayerList } from "./player-list"
import { GameStartingCountdown } from "./starting-countdown"

export const GamePlayLobbyScreen = () => {
  const session = useGameSession()
  const state = useGameState()
  const { start } = useGameSocket()

  if (!session) return null

  const isHost = session.role == "host"

  return (
    <div className="flex w-full flex-col gap-6">
      {state.type === "STARTING" && (
        <GameStartingCountdown
          durationMs={state.startingInMs}
          isHost={isHost}
        />
      )}

      <div className="flex flex-col items-center gap-2">
        {/* Game Code */}
        <p className="font-semibold tracking-widest text-muted-foreground">
          Game Code
        </p>
        <p className="text-mono text-9xl font-black tracking-widest text-foreground">
          {session.gameCode}
        </p>

        {/* Game State (waiting for players) */}
        <Badge variant="secondary" className="gap-1.5">
          <span className="relative flex h-2 w-2">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-400 opacity-75"></span>
          </span>
          Waiting for players...
        </Badge>
      </div>

      {/* Joined Players */}
      <Card>
        <CardContent className="pt-4">
          <PlayerList />
        </CardContent>
      </Card>

      {/* Host Controls */}
      {isHost && (
        <div className="flex gap-3">
          <Button size="lg" className="flex-1" onClick={start}>
            Start Game
          </Button>
        </div>
      )}
    </div>
  )
}
