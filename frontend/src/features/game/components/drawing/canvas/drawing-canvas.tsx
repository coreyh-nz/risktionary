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
  onStrokeStart: (point: DrawingPoint, tool: DrawingTool, color: string) => void
  onStrokePoint: (point: DrawingPoint) => void
  onStrokeEnd: (stroke: DrawingStroke) => void
}

export const DrawingCanvas = ({
  onStrokeStart,
  onStrokePoint,
  onStrokeEnd,
}: DrawingCanvasProps) => {
  const tool = useDrawingTool()
  const colour = useDrawingColour()
  const {
    canvasRef,
    containerRef,
    canvasReady,
    handleStart,
    handleMove,
    handleEnd,
  } = useDrawingCanvas({
    tool,
    colour,
    onStrokeStart,
    onStrokePoint,
    onStrokeEnd,
  })

  return (
    <Canvas
      containerRef={containerRef}
      canvasRef={canvasRef}
      canvasReady={canvasReady}
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
