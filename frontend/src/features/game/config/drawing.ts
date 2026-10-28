import type { StrokeOptions } from "perfect-freehand"
import { PALETTE_COLOURS_LIST } from "../lib/colors"
import {
  DrawingColour,
  DrawingTool,
} from "../types/round/phase/drawing/drawing"

export const DRAWING_ASPECT_RATIO = 4 / 3

export const CANVAS_BACKGROUND_COLOUR = "#FFFFFF"

export const BRUSH_SIZE = 12
export const ERASER_SIZE_MULTIPLIER = 2.8

export const STROKE_OPTIONS: Omit<StrokeOptions, "size"> = {
  thinning: 0.3,
  smoothing: 0.6,
  streamline: 0.5,
  simulatePressure: false,
  last: true,
  start: { taper: 0, cap: true },
  end: { taper: 10, cap: true },
}

export const DEFAULT_DRAWING_TOOL: DrawingTool = "PEN"
export const DEFAULT_DRAWING_COLOUR: DrawingColour = PALETTE_COLOURS_LIST[0]

export const STROKE_FLUSH_FPS = 30
export const STROKE_FLUSH_INTERVAL_MS = 1000 / STROKE_FLUSH_FPS

export const SNAPSHOT_INTERVAL_MS = 10000
export const SNAPSHOT_IMAGE_TYPE = "image/png"
export const SNAPSHOT_MAX_DIMENSION = 1024
