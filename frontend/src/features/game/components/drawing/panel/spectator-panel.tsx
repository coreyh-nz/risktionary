import { Canvas } from "@/features/game/components/drawing/canvas/canvas"
import { useCanvasRenderer } from "@/features/game/hooks/canvas/use-canvas-renderer"
import { useGameSocketDrawingEvents } from "@/features/game/hooks/socket/use-game-socket-drawing-events"

export const SpectatorPanel = () => {
  const {
    canvasRef,
    containerRef,
    canvasReady,
    startRemoteStroke,
    addRemotePoints,
    commitRemoteStroke,
  } = useCanvasRenderer()
  useGameSocketDrawingEvents({
    onRemoteStrokeStart: startRemoteStroke,
    onRemoteStrokePoints: addRemotePoints,
    onRemoteStrokeEnd: commitRemoteStroke,
  })

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
