"use client"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { useGameStore } from "@/stores/game-store"
import { PlayerList } from "./player-list"

export const GamePlayLobbyScreen = () => {
  const { session } = useGameStore()

  if (!session) return

  return (
    <div className="flex w-full flex-col gap-6">
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
      {session.role === "host" && (
        <div className="flex gap-3">
          <Button size="lg" className="flex-1">
            Start Game
          </Button>
        </div>
      )}
    </div>
  )
}
