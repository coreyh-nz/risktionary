import { useWebSocket } from "@/providers/web-socket-provider"
import { useCallback, useEffect, useRef } from "react"
import { DrawingPoint, DrawingTool } from "../../types/drawing/drawing"
import {
  DrawingStrokeEndCommand,
  DrawingStrokePointsCommand,
  DrawingStrokeStartCommand,
} from "../../types/drawing/drawing-commands"

const FLUSH_FPS = 30
const FLUSH_INTERVAL_MS = 1000 / FLUSH_FPS

export const useGameSocketDrawingCommands = () => {
  const { send } = useWebSocket()

  const pointQueue = useRef<DrawingPoint[]>([])
  const flushInterval = useRef<ReturnType<typeof setInterval> | null>(null)

  // flushes whatever has accumulated in the queue as a single STROKE_POINTS command.
  // called on a fixed interval while drawing.
  const flushQueue = useCallback(() => {
    if (pointQueue.current.length === 0) return

    const command: DrawingStrokePointsCommand = {
      type: "STROKE_POINTS",
      points: [...pointQueue.current], // copy to avoid reference issues
    }

    pointQueue.current = []
    send("/app/game/draw", command)
  }, [send])

  const startFlushLoop = useCallback(() => {
    if (flushInterval.current !== null) return
    flushInterval.current = setInterval(flushQueue, FLUSH_INTERVAL_MS)
  }, [flushQueue])

  const stopFlushLoop = useCallback(() => {
    if (flushInterval.current === null) return
    clearInterval(flushInterval.current)
    flushInterval.current = null
  }, [])

  // stop flush loop on unmount
  useEffect(() => {
    return () => stopFlushLoop()
  }, [stopFlushLoop])

  const onStrokeStart = useCallback(
    (point: DrawingPoint, tool: DrawingTool, colour: string) => {
      const command: DrawingStrokeStartCommand = {
        type: "STROKE_START",
        point,
        tool,
        colour,
      }
      send(`/app/game/draw`, command)

      startFlushLoop()
    },
    [send, startFlushLoop]
  )

  const onStrokePoint = useCallback((points: DrawingPoint[]) => {
    pointQueue.current.push(...points)
  }, [])

  const onStrokeEnd = useCallback(() => {
    // no need to send remaining points as they will be replaced by stroke
    stopFlushLoop()
    pointQueue.current = []

    const command: DrawingStrokeEndCommand = {
      type: "STROKE_END",
    }
    send(`/app/game/draw`, command)
  }, [send, stopFlushLoop])

  return { onStrokeStart, onStrokePoint, onStrokeEnd }
}
