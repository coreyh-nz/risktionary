"use client"

import { Button } from "@/components/ui/button"
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip"
import { cn } from "cn"

import { Eraser, Pencil } from "lucide-react"
import {
  useDrawingSetTool,
  useDrawingTool,
} from "../../../../../stores/drawing-store"

export const DrawingToolbar = () => {
  const currentTool = useDrawingTool()
  const setTool = useDrawingSetTool()

  return (
    <TooltipProvider delay={300}>
      <div className="flex flex-wrap items-center gap-2">
        <Tooltip>
          <TooltipTrigger
            render={
              <Button
                variant={currentTool === "PEN" ? "default" : "outline"}
                size="icon"
                onClick={() => setTool("PEN")}
                className={cn(
                  "size-10 transition-all",
                  currentTool === "PEN" && "shadow-md"
                )}
                aria-label="Pen tool"
              >
                <Pencil className="size-4" />
              </Button>
            }
          ></TooltipTrigger>
          <TooltipContent side="bottom" className="text-xs">
            Pen
          </TooltipContent>
        </Tooltip>

        <Tooltip>
          <TooltipTrigger
            render={
              <Button
                variant={currentTool === "ERASER" ? "default" : "outline"}
                size="icon"
                onClick={() => setTool("ERASER")}
                className={cn(
                  "size-10 transition-all",
                  currentTool === "ERASER" && "shadow-md"
                )}
                aria-label="Eraser tool"
              >
                <Eraser className="size-4" />
              </Button>
            }
          />
          <TooltipContent side="bottom" className="text-xs">
            Eraser
          </TooltipContent>
        </Tooltip>
      </div>
    </TooltipProvider>
  )
}
