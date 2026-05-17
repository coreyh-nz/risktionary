import { Toolbar } from "@/features/game/components/drawing/toolbar"
import ColourPalette from "@/features/game/components/drawing/colour-palette"
import { Separator } from "@/components/ui/separator"
import { DrawingCanvas } from "../canvas/drawing-canvas"
import { useGameSocketDrawingCommands } from "@/features/game/hooks/socket/use-game-socket-drawing-commands"

export const DrawerPanel = () => {
  const { onStrokeStart, onStrokePoint, onStrokeEnd } =
    useGameSocketDrawingCommands()

  return (
    <div className="flex flex-1 flex-col gap-3 p-3">
      <div className="flex items-center gap-2 rounded-xl border border-border bg-card p-4">
        <Toolbar />
        <Separator orientation="vertical" className="my-2" />
        <ColourPalette />
      </div>
      <DrawingCanvas
        onStrokeStart={onStrokeStart}
        onStrokePoint={(point) => onStrokePoint([point])} // TODO: - fix
        onStrokeEnd={onStrokeEnd}
      />
    </div>
  )
}
