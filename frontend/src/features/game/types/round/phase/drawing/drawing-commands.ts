import { DrawingPoint, DrawingTool } from "./drawing"

export type DrawingCommandType =
  | "STROKE_START"
  | "STROKE_POINTS"
  | "STROKE_END"
  | "CANVAS_CLEAR"

interface DrawingCommand {
  type: DrawingCommandType
}

export interface DrawingStrokeStartCommand extends DrawingCommand {
  type: "STROKE_START"
  point: DrawingPoint
  tool: DrawingTool
  colour: string
}

export interface DrawingStrokePointsCommand extends DrawingCommand {
  type: "STROKE_POINTS"
  points: DrawingPoint[]
}

export interface DrawingStrokeEndCommand extends DrawingCommand {
  type: "STROKE_END"
}

export interface DrawingCanvasClearCommand extends DrawingCommand {
  type: "CANVAS_CLEAR"
}
