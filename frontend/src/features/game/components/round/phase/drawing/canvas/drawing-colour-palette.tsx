import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip"
import { cn } from "@/lib/utils"
import { PALETTE_COLOURS_LIST } from "../../../../../lib/colors"
import {
  useDrawingColour,
  useDrawingSetColour,
  useDrawingTool,
} from "../../../../../stores/drawing-store"

const DrawingColourPalette = () => {
  const currentTool = useDrawingTool()
  const currentColour = useDrawingColour()
  const setColour = useDrawingSetColour()

  const isDisabled = currentTool !== "PEN"

  return (
    <TooltipProvider delay={300}>
      <div className="flex flex-wrap gap-2">
        {PALETTE_COLOURS_LIST.map((colour) => {
          const isActive =
            currentTool === "PEN" && currentColour.value === colour.value

          return (
            <Tooltip key={colour.value}>
              <TooltipTrigger
                render={
                  <button
                    onClick={() => setColour(colour)}
                    disabled={isDisabled}
                    className={cn(
                      "size-8 rounded-lg border-2 transition-all duration-150 focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:outline-none",
                      isActive
                        ? "scale-110 border-foreground shadow-md"
                        : "border-transparent shadow-sm",
                      !isDisabled && "cursor-pointer hover:scale-110",
                      isDisabled && "cursor-not-allowed opacity-50"
                    )}
                    style={{ backgroundColor: colour.value }}
                    aria-label={`Select ${colour.name}`}
                  />
                }
              />
              <TooltipContent side="bottom" className="text-xs">
                {colour.name}
              </TooltipContent>
            </Tooltip>
          )
        })}
      </div>
    </TooltipProvider>
  )
}

export default DrawingColourPalette
