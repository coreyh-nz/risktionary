import { Card, CardContent } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import ColourPalette from "@/features/game/components/drawing/colour-palette"
import { Toolbar } from "@/features/game/components/drawing/toolbar"
import { useConstrainedCanvasSize } from "@/features/game/hooks/canvas/use-constrained-canvas-size"
import { useGameSocketDrawingCommands } from "@/features/game/hooks/socket/use-game-socket-drawing-commands"
import { DrawingCanvas } from "../canvas/drawing-canvas"

const DrawerCanvas = ({ width, height }: { width: number; height: number }) => {
  const { onStrokeStart, onStrokePoint, onStrokeEnd } =
    useGameSocketDrawingCommands()

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
            <Toolbar />
            <Separator orientation="vertical" />
            <ColourPalette />
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
