import {
  DrawingPoint,
  DrawingStroke,
  DrawingTool,
} from "@/features/game/types/round/phase/drawing/drawing"
import { MouseEvent, TouchEvent, useCallback, useEffect, useRef } from "react"
import {
  drawingStore,
  useDrawingColour,
  useDrawingTool,
} from "../../stores/drawing-store"
import { useCanvasRenderer } from "./use-canvas-renderer"

interface UseDrawingCanvasOptions {
  width: number
  height: number
  tool: DrawingTool
  colour: { value: string }
  onStrokeStart?: (
    point: DrawingPoint,
    tool: DrawingTool,
    colour: string
  ) => void
  onStrokePoint?: (point: DrawingPoint) => void
  onStrokeEnd?: (stroke: DrawingStroke) => void
}

export const useDrawingCanvas = ({
  width,
  height,
  onStrokeStart,
  onStrokePoint,
  onStrokeEnd,
}: UseDrawingCanvasOptions) => {
  const { canvasRef, render } = useCanvasRenderer(width, height)
  const tool = useDrawingTool()
  const colour = useDrawingColour()

  const isDrawingRef = useRef(false)
  const toolRef = useRef(tool)
  const colourRef = useRef(colour)
  const onStrokeStartRef = useRef(onStrokeStart)
  const onStrokePointRef = useRef(onStrokePoint)
  const onStrokeEndRef = useRef(onStrokeEnd)

  useEffect(() => {
    toolRef.current = tool
    colourRef.current = colour
    onStrokeStartRef.current = onStrokeStart
    onStrokePointRef.current = onStrokePoint
    onStrokeEndRef.current = onStrokeEnd
  }, [colour, onStrokeEnd, onStrokePoint, onStrokeStart, tool])

  const getCanvasPoint = useCallback(
    (e: MouseEvent | TouchEvent): DrawingPoint | null => {
      const canvas = canvasRef.current
      if (!canvas) return null

      const rect = canvas.getBoundingClientRect()
      let clientX: number, clientY: number

      if ("touches" in e) {
        if (e.touches.length === 0) return null
        clientX = e.touches[0].clientX
        clientY = e.touches[0].clientY
      } else {
        clientX = (e as MouseEvent).clientX
        clientY = (e as MouseEvent).clientY
      }

      return { x: clientX - rect.left, y: clientY - rect.top }
    },
    [canvasRef]
  )

  const handleStart = useCallback(
    (e: MouseEvent | TouchEvent) => {
      e.preventDefault()

      const point = getCanvasPoint(e)
      if (!point) return

      isDrawingRef.current = true
      drawingStore
        .getState()
        .startLocalStroke(point, colourRef.current.value, toolRef.current)
      onStrokeStartRef.current?.(
        point,
        toolRef.current,
        colourRef.current.value
      )
    },
    [getCanvasPoint]
  )

  const handleMove = useCallback(
    (e: MouseEvent | TouchEvent) => {
      if (!isDrawingRef.current) return

      e.preventDefault()

      const point = getCanvasPoint(e)
      if (!point) return

      drawingStore.getState().addLocalPoint(point)
      onStrokePointRef.current?.(point)
    },
    [getCanvasPoint]
  )

  const handleEnd = useCallback(() => {
    if (!isDrawingRef.current) return

    isDrawingRef.current = false

    const stroke = drawingStore.getState().commitLocalStroke()
    if (stroke) {
      onStrokeEndRef.current?.(stroke)
    }
  }, [])

  return {
    canvasRef,
    render,
    handleStart,
    handleMove,
    handleEnd,
  }
}
