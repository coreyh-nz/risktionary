import { useCanvasRenderer } from "@/features/game/hooks/canvas/use-canvas-renderer"
import { useCanvasSnapshot } from "@/features/game/hooks/canvas/use-canvas-snapshot"
import { useConstrainedCanvasSize } from "@/features/game/hooks/canvas/use-constrained-canvas-size"
import { useDrawingEvents } from "@/features/game/hooks/round/phase/drawing/use-drawing-events"
import { useDrawingSnapshotCommands } from "@/features/game/hooks/round/phase/drawing/use-drawing-snapshot-commands"
import { Canvas } from "../canvas/canvas"

const HostCanvas = ({ width, height }: { width: number; height: number }) => {
  const { canvasRef } = useCanvasRenderer(width, height)
  const { sendSnapshot } = useDrawingSnapshotCommands()
  useDrawingEvents()
  useCanvasSnapshot(canvasRef, sendSnapshot)

  return (
    <div style={{ width, height }} className="shrink-0">
      <Canvas canvasRef={canvasRef} />
    </div>
  )
}

export const HostPanel = () => {
  const { wrapperRef, size } = useConstrainedCanvasSize()
  return (
    <div
      ref={wrapperRef}
      className="flex flex-col flex-1 min-h-0 min-w-0 items-center"
    >
      {size && <HostCanvas width={size.width} height={size.height} />}
    </div>
  )
}
