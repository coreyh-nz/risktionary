import { useWebSocket } from "@/providers/web-socket-provider"
import { useGameSession } from "../../stores/game-store-selectors"
import { DrawingPoint, DrawingTool } from "../../types/drawing/drawing"
import { useCallback, useEffect } from "react"
import { stompHandler } from "@/lib/stomp-utils"
import {
  DrawingEvent,
  DrawingStrokeEndEvent,
  DrawingStrokePointsEvent,
  DrawingStrokeStartEvent,
} from "../../types/drawing/drawing-events"
import { useAuth } from "@/providers/auth-provider"

interface UseGameSocketDrawingEventsOptions {
  onRemoteStrokeStart: (
    drawerId: string,
    tool: DrawingTool,
    point: DrawingPoint,
    colour: string
  ) => void
  onRemoteStrokePoints: (drawerId: string, points: DrawingPoint[]) => void
  onRemoteStrokeEnd: (drawerId: string) => void
}

export const useGameSocketDrawingEvents = ({
  onRemoteStrokeStart,
  onRemoteStrokePoints,
  onRemoteStrokeEnd,
}: UseGameSocketDrawingEventsOptions) => {
  const { subscribe } = useWebSocket()
  const session = useGameSession()
  const { user } = useAuth()
  const gameId = session?.gameId

  const handleStrokeStartEvent = useCallback(
    ({ drawerId, tool, point, colour }: DrawingStrokeStartEvent) => {
      onRemoteStrokeStart(drawerId, tool, point, colour)
    },
    [onRemoteStrokeStart]
  )

  const handleStrokePointsEvent = useCallback(
    ({ drawerId, points }: DrawingStrokePointsEvent) => {
      onRemoteStrokePoints(drawerId, points)
    },
    [onRemoteStrokePoints]
  )

  const handleStrokeEndEvent = useCallback(
    ({ drawerId }: DrawingStrokeEndEvent) => {
      onRemoteStrokeEnd(drawerId)
    },
    [onRemoteStrokeEnd]
  )

  const handleCanvasClear = useCallback(() => {}, [])

  useEffect(() => {
    if (!session) return

    const subscription = subscribe(
      `/topic/game/${gameId}/draw`,
      stompHandler<DrawingEvent>((message) => {
        // if event is from our command, don't handle it
        const drawerId = message.drawerId
        if (session.role === "player" && session.playerId == drawerId) return
        if (session.role === "host" && user && user.id == drawerId) return

        switch (message.type) {
          case "STROKE_START":
            handleStrokeStartEvent(message as DrawingStrokeStartEvent)
            break
          case "STROKE_POINTS":
            handleStrokePointsEvent(message as DrawingStrokePointsEvent)
            break
          case "STROKE_END":
            handleStrokeEndEvent(message as DrawingStrokeEndEvent)
            break
          case "CANVAS_CLEAR":
            handleCanvasClear()
            break
        }
      })
    )
    return () => subscription && subscription.unsubscribe()
  }, [
    subscribe,
    gameId,
    handleStrokeStartEvent,
    handleStrokePointsEvent,
    handleStrokeEndEvent,
    handleCanvasClear,
    session,
    user,
  ])
}
