import { useCallback, useEffect, useRef, useState } from "react"
import { getStroke } from "perfect-freehand"
import {
  DrawingPoint,
  DrawingStroke,
  DrawingTool,
} from "../../types/drawing/drawing"

const BRUSH_SIZE = 12
const ERASER_MULTIPLIER = 2.8

const STROKE_OPTIONS = {
  thinning: 0.3,
  smoothing: 0.6,
  streamline: 0.5,
  simulatePressure: false,
  last: true,
  start: { taper: 0, cap: true },
  end: { taper: 10, cap: true },
}

const drawPerfectStroke = (
  ctx: CanvasRenderingContext2D,
  points: DrawingPoint[],
  colour: string,
  isEraser: boolean
) => {
  if (points.length === 0) return

  const stroke = getStroke(
    points.map((p) => [p.x, p.y] as [number, number]),
    {
      ...STROKE_OPTIONS,
      size: isEraser ? BRUSH_SIZE * ERASER_MULTIPLIER : BRUSH_SIZE,
    }
  )

  if (stroke.length < 2) return

  ctx.save()
  ctx.fillStyle = isEraser ? "#FFFFFF" : colour
  ctx.beginPath()
  ctx.moveTo(stroke[0][0], stroke[0][1])
  for (let i = 1; i < stroke.length; i++) {
    ctx.lineTo(stroke[i][0], stroke[i][1])
  }
  ctx.closePath()
  ctx.fill()
  ctx.restore()
}

interface DrawingState {
  points: DrawingPoint[]
  colour: string
  tool: DrawingTool
}

export const useCanvasRenderer = () => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null)
  const containerRef = useRef<HTMLDivElement | null>(null)
  const rafRef = useRef<number | null>(null)
  const dimensionsRef = useRef({ width: 0, height: 0 })

  const strokesRef = useRef<DrawingStroke[]>([])
  const localDrawingRef = useRef<DrawingState | null>(null)
  const remoteDrawersRef = useRef<Map<string, DrawingState>>(new Map())

  const [canvasReady, setCanvasReady] = useState(false)

  const render = useCallback(() => {
    if (rafRef.current !== null) return
    rafRef.current = requestAnimationFrame(() => {
      rafRef.current = null
      const canvas = canvasRef.current
      if (!canvas) return
      const ctx = canvas.getContext("2d")
      if (!ctx) return

      const { width, height } = dimensionsRef.current
      const dpr = window.devicePixelRatio || 1

      ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
      ctx.fillStyle = "#FFFFFF"
      ctx.fillRect(0, 0, width, height)

      // committed strokes
      for (const s of strokesRef.current) {
        drawPerfectStroke(ctx, s.points, s.colour, s.tool === "ERASER")
      }

      // remote in-progress strokes
      for (const state of remoteDrawersRef.current.values()) {
        if (state.points.length > 0) {
          drawPerfectStroke(
            ctx,
            state.points,
            state.colour,
            state.tool === "ERASER"
          )
        }
      }

      // local in-progress stroke
      if (
        localDrawingRef.current &&
        localDrawingRef.current.points.length > 0
      ) {
        const l = localDrawingRef.current
        drawPerfectStroke(ctx, l.points, l.colour, l.tool === "ERASER")
      }
    })
  }, [])

  const resizeCanvas = useCallback(() => {
    const canvas = canvasRef.current
    const container = containerRef.current
    if (!canvas || !container) return

    const ctx = canvas.getContext("2d", { alpha: false })
    if (!ctx) return

    const rect = container.getBoundingClientRect()
    const dpr = window.devicePixelRatio || 1

    canvas.width = rect.width * dpr
    canvas.height = rect.height * dpr
    canvas.style.width = `${rect.width}px`
    canvas.style.height = `${rect.height}px`

    ctx.scale(dpr, dpr)
    ctx.imageSmoothingEnabled = true
    ctx.imageSmoothingQuality = "high"
    ctx.fillStyle = "#FFFFFF"
    ctx.fillRect(0, 0, rect.width, rect.height)

    dimensionsRef.current = { width: rect.width, height: rect.height }
    setCanvasReady(true)
    render()
  }, [render])

  useEffect(() => {
    resizeCanvas()
    window.addEventListener("resize", resizeCanvas)
    return () => window.removeEventListener("resize", resizeCanvas)
  }, [resizeCanvas])

  // local drawing
  const startLocalStroke = useCallback(
    (point: DrawingPoint, colour: string, tool: DrawingTool) => {
      localDrawingRef.current = { points: [point], colour, tool }
      render()
    },
    [render]
  )

  const addLocalPoint = useCallback(
    (point: DrawingPoint) => {
      if (!localDrawingRef.current) return
      localDrawingRef.current.points.push(point)
      render()
    },
    [render]
  )

  // commits the in-progress local stroke to the permanent record and returns
  // it so the drawing hook can fire onStrokeEnd and broadcast.
  const commitLocalStroke = useCallback((): DrawingStroke | null => {
    const current = localDrawingRef.current
    localDrawingRef.current = null

    if (!current || current.points.length === 0) {
      render()
      return null
    }

    const stroke: DrawingStroke = {
      points: current.points,
      colour: current.colour,
      tool: current.tool,
    }

    strokesRef.current = [...strokesRef.current, stroke]
    render()
    return stroke
  }, [render])

  // remote drawing
  const startRemoteStroke = useCallback(
    (
      userId: string,
      tool: DrawingTool,
      point: DrawingPoint,
      colour: string
    ) => {
      remoteDrawersRef.current = new Map(remoteDrawersRef.current).set(userId, {
        points: [point],
        colour,
        tool,
      })
      render()
    },
    [render]
  )

  const addRemotePoints = useCallback(
    (userId: string, points: DrawingPoint[]) => {
      const existing = remoteDrawersRef.current.get(userId)
      if (!existing) return
      remoteDrawersRef.current = new Map(remoteDrawersRef.current).set(userId, {
        ...existing,
        points: [...existing.points, ...points],
      })
      render()
    },
    [render]
  )

  const commitRemoteStroke = useCallback(
    (userId: string) => {
      const existing = remoteDrawersRef.current.get(userId)
      if (!existing || existing.points.length === 0) return

      const stroke: DrawingStroke = {
        points: existing.points,
        colour: existing.colour,
        tool: existing.tool,
      }

      remoteDrawersRef.current = new Map(remoteDrawersRef.current)
      remoteDrawersRef.current.delete(userId)
      strokesRef.current = [...strokesRef.current, stroke]
      render()
    },
    [render]
  )

  const clearRemoteDrawer = useCallback(
    (userId: string) => {
      remoteDrawersRef.current = new Map(remoteDrawersRef.current)
      remoteDrawersRef.current.delete(userId)
      render()
    },
    [render]
  )

  // canvas-wide
  const clearCanvas = useCallback(() => {
    strokesRef.current = []
    localDrawingRef.current = null
    remoteDrawersRef.current = new Map()
    render()
  }, [render])

  return {
    // refs
    canvasRef,
    containerRef,
    canvasReady,
    strokesRef,

    // local stroke lifecycle
    startLocalStroke,
    addLocalPoint,
    commitLocalStroke,

    // remote stroke lifecycle
    startRemoteStroke,
    addRemotePoints,
    commitRemoteStroke,
    clearRemoteDrawer,

    // canvas-wide
    clearCanvas,
    render,
  }
}
