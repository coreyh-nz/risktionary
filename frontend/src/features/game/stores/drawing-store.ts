import { create } from "zustand"
import { DrawingColour, DrawingTool } from "../types/drawing/drawing"
import { PALETTE_COLOURS } from "../lib/colors"
import { useShallow } from "zustand/shallow"

interface DrawingStore {
  tool: DrawingTool
  colour: DrawingColour

  setTool: (tool: DrawingTool) => void
  setColour: (colour: DrawingColour) => void
}

export const useDrawingStore = create<DrawingStore>((set) => ({
  tool: "PEN",
  colour: PALETTE_COLOURS.BLACK,

  setTool: (tool) => set({ tool }),

  setColour: (colour) => set({ colour }),
}))

export const useDrawingTool = () => useDrawingStore((s) => s.tool)

export const useDrawingColour = () =>
  useDrawingStore(useShallow((s) => s.colour))

export const useDrawingSetTool = () => useDrawingStore((s) => s.setTool)

export const useDrawingSetColour = () => useDrawingStore((s) => s.setColour)
