import { drawingStore } from "@/features/game/stores/drawing-store"
import {
  DrawingEvent,
  DrawingStrokePointsEvent,
  DrawingStrokeStartEvent,
} from "@/features/game/types/round/phase/drawing/drawing-events"
import { stompHandler } from "@/lib/stomp-utils"
import { useAuth } from "@/providers/auth-provider"
import { useWebSocket } from "@/providers/web-socket-provider"
import { useEffect } from "react"
import { useGameSession } from "../../../../stores/game-store-selectors"

export const useDrawingEvents = () => {
  const { subscribe } = useWebSocket()
  const session = useGameSession()
  const { user } = useAuth()
  const gameId = session?.gameId

  useEffect(() => {
    if (!session) return

    const subscription = subscribe(
      `/topic/game/${gameId}/draw`,
      stompHandler<DrawingEvent>((message) => {
        const drawerId = message.drawerId
        if (session.role === "player" && session.playerId == drawerId) return
        if (session.role === "host" && user && user.id == drawerId) return

        const store = drawingStore.getState()
        switch (message.type) {
          case "STROKE_START": {
            const { tool, point, colour } = message as DrawingStrokeStartEvent
            store.startRemoteStroke(drawerId, tool, point, colour)
            break
          }
          case "STROKE_POINTS":
            store.addRemotePoints(
              drawerId,
              (message as DrawingStrokePointsEvent).points
            )
            break
          case "STROKE_END":
            store.commitRemoteStroke(drawerId)
            break
          case "CANVAS_CLEAR":
            store.clearCanvas()
            break
        }
      })
    )

    return () => subscription && subscription.unsubscribe()
  }, [subscribe, gameId, session, user])
}
