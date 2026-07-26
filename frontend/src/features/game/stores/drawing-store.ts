import {
  DrawingColour,
  DrawingTool,
} from "@/features/game/types/round/phase/drawing/drawing"
import { create } from "zustand"
import { useShallow } from "zustand/shallow"
import { PALETTE_COLOURS } from "../lib/colors"

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
