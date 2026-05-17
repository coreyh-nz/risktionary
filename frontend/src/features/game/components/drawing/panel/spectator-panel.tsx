import { Canvas } from "@/features/game/components/drawing/canvas/canvas"
import { useCanvasRenderer } from "@/features/game/hooks/canvas/use-canvas-renderer"

export const SpectatorPanel = () => {
  const { canvasRef, containerRef, canvasReady } = useCanvasRenderer()
  return (
    <div className="flex flex-1 p-3">
      <Canvas
        containerRef={containerRef}
        canvasRef={canvasRef}
        canvasReady={canvasReady}
      />
    </div>
  )
}
