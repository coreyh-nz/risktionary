import { Canvas } from "@/features/game/components/round/phase/drawing/canvas/canvas"
import { useCanvasRenderer } from "@/features/game/hooks/canvas/use-canvas-renderer"
import { useConstrainedCanvasSize } from "@/features/game/hooks/canvas/use-constrained-canvas-size"
import { useDrawingEvents } from "@/features/game/hooks/round/phase/drawing/use-drawing-events"

const SpectatorCanvas = ({
  width,
  height,
}: {
  width: number
  height: number
}) => {
  const { canvasRef } = useCanvasRenderer(width, height)
  useDrawingEvents()
  return (
    <div style={{ width, height }} className="shrink-0">
      <Canvas canvasRef={canvasRef} />
    </div>
  )
}

export const SpectatorPanel = () => {
  const { wrapperRef, size } = useConstrainedCanvasSize()

  return (
    <div
      ref={wrapperRef}
      className="flex flex-col flex-1 min-h-0 min-w-0 items-center"
    >
      {size && <SpectatorCanvas width={size.width} height={size.height} />}
    </div>
  )
}
