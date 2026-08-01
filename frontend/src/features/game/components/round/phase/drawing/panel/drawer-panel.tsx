import { Card, CardContent } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { DrawingCanvas } from "@/features/game/components/round/phase/drawing/canvas/drawing-canvas"
import DrawingColourPalette from "@/features/game/components/round/phase/drawing/canvas/drawing-colour-palette"
import { DrawingToolbar } from "@/features/game/components/round/phase/drawing/canvas/drawing-toolbar"
import { useConstrainedCanvasSize } from "@/features/game/hooks/canvas/use-constrained-canvas-size"
import { useDrawingCommands } from "@/features/game/hooks/round/phase/drawing/use-drawing-commands"

const DrawerCanvas = ({ width, height }: { width: number; height: number }) => {
  const { onStrokeStart, onStrokePoint, onStrokeEnd } = useDrawingCommands()

  return (
    <div style={{ width, height }} className="shrink-0">
      <DrawingCanvas
        width={width}
        height={height}
        onStrokeStart={onStrokeStart}
        onStrokePoint={(point) => onStrokePoint([point])}
        onStrokeEnd={onStrokeEnd}
      />
    </div>
  )
}

export const DrawerPanel = () => {
  const { wrapperRef, toolbarRef, size } = useConstrainedCanvasSize()

  return (
    <div
      ref={wrapperRef}
      className="flex flex-col flex-1 min-h-0 min-w-0 items-center"
    >
      {size && <DrawerCanvas width={size.width} height={size.height} />}
      <div ref={toolbarRef} className="shrink-0 mt-3">
        <Card>
          <CardContent className="flex gap-2 items-center">
            <DrawingToolbar />
            <Separator orientation="vertical" />
            <DrawingColourPalette />
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
