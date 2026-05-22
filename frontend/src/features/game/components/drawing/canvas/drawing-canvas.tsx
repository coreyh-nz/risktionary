import { useDrawingCanvas } from "@/features/game/hooks/canvas/use-canvas-drawing"
import {
  useDrawingColour,
  useDrawingTool,
} from "@/features/game/stores/drawing-store"
import {
  DrawingPoint,
  DrawingStroke,
  DrawingTool,
} from "@/features/game/types/drawing/drawing"
import { Canvas } from "./canvas"

interface DrawingCanvasProps {
  height: number
  width: number
  onStrokeStart: (point: DrawingPoint, tool: DrawingTool, color: string) => void
  onStrokePoint: (point: DrawingPoint) => void
  onStrokeEnd: (stroke: DrawingStroke) => void
}

export const DrawingCanvas = ({
  width,
  height,
  onStrokeStart,
  onStrokePoint,
  onStrokeEnd,
}: DrawingCanvasProps) => {
  const tool = useDrawingTool()
  const colour = useDrawingColour()
  const { canvasRef, handleStart, handleMove, handleEnd } = useDrawingCanvas({
    width,
    height,
    tool,
    colour,
    onStrokeStart,
    onStrokePoint,
    onStrokeEnd,
  })

  return (
    <Canvas
      canvasRef={canvasRef}
      onMouseDown={handleStart}
      onMouseMove={handleMove}
      onMouseUp={handleEnd}
      onMouseLeave={handleEnd}
      onTouchStart={handleStart}
      onTouchMove={handleMove}
      onTouchEnd={handleEnd}
    />
  )
}
