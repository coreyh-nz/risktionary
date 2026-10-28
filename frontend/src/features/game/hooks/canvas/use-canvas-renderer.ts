import { getStroke } from "perfect-freehand"
import { useCallback, useEffect, useLayoutEffect, useRef } from "react"
import {
  BRUSH_SIZE,
  CANVAS_BACKGROUND_COLOUR,
  ERASER_SIZE_MULTIPLIER,
  STROKE_OPTIONS,
} from "../../config/drawing"
import { drawingStore } from "../../stores/drawing-store"
import { DrawingPoint } from "../../types/round/phase/drawing/drawing"

const drawStroke = (
  ctx: CanvasRenderingContext2D,
  points: DrawingPoint[],
  colour: string,
  isEraser: boolean,
  width: number,
  height: number
) => {
  if (points.length === 0) return

  const stroke = getStroke(
    points.map((p) => [p.x * width, p.y * height]),
    {
      ...STROKE_OPTIONS,
      size: isEraser ? BRUSH_SIZE * ERASER_SIZE_MULTIPLIER : BRUSH_SIZE,
    }
  )

  if (stroke.length < 2) return

  ctx.save()
  ctx.fillStyle = isEraser ? CANVAS_BACKGROUND_COLOUR : colour
  ctx.beginPath()
  ctx.moveTo(stroke[0][0], stroke[0][1])
  for (let i = 1; i < stroke.length; i++) {
    ctx.lineTo(stroke[i][0], stroke[i][1])
  }
  ctx.closePath()
  ctx.fill()
  ctx.restore()
}

export const useCanvasRenderer = (width: number, height: number) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null)
  const rafRef = useRef<number | null>(null)

  const render = useCallback(() => {
    if (rafRef.current !== null) return
    rafRef.current = requestAnimationFrame(() => {
      rafRef.current = null
      const canvas = canvasRef.current
      if (!canvas) return

      const ctx = canvas.getContext("2d")
      if (!ctx) return

      const dpr = window.devicePixelRatio || 1
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
      ctx.fillStyle = CANVAS_BACKGROUND_COLOUR
      ctx.fillRect(0, 0, width, height)

      const { strokes, remoteDrawers, localDrawing } = drawingStore.getState()

      // committed strokes
      for (const s of strokes) {
        drawStroke(ctx, s.points, s.colour, s.tool === "ERASER", width, height)
      }

      // remote in-progress strokes
      for (const state of remoteDrawers.values()) {
        if (state.points.length > 0)
          drawStroke(
            ctx,
            state.points,
            state.colour,
            state.tool === "ERASER",
            width,
            height
          )
      }

      // local in-progress stroke
      if (localDrawing && localDrawing.points.length > 0) {
        drawStroke(
          ctx,
          localDrawing.points,
          localDrawing.colour,
          localDrawing.tool === "ERASER",
          width,
          height
        )
      }
    })
  }, [width, height])

  // repaint on every store change
  useEffect(() => drawingStore.subscribe(render), [render])

  useLayoutEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const dpr = window.devicePixelRatio || 1
    canvas.width = width * dpr
    canvas.height = height * dpr
    canvas.style.width = `${width}px`
    canvas.style.height = `${height}px`

    const ctx = canvas.getContext("2d", { alpha: false })
    if (!ctx) return

    ctx.scale(dpr, dpr)
    ctx.imageSmoothingEnabled = true
    ctx.imageSmoothingQuality = "high"
    ctx.fillStyle = CANVAS_BACKGROUND_COLOUR
    ctx.fillRect(0, 0, width, height)
    render()
  }, [width, height, render])

  return { canvasRef, render }
}
