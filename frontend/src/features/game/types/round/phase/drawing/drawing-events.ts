import { DrawingPoint, DrawingTool } from "./drawing"

export type DrawingEventType =
  | "STROKE_START"
  | "STROKE_POINTS"
  | "STROKE_END"
  | "CANVAS_CLEAR"

export interface DrawingEvent {
  type: DrawingEventType
  drawerId: string
}

export interface DrawingStrokeStartEvent extends DrawingEvent {
  type: "STROKE_START"
  point: DrawingPoint
  tool: DrawingTool
  colour: string
}

export interface DrawingStrokePointsEvent extends DrawingEvent {
  type: "STROKE_POINTS"
  points: DrawingPoint[]
}

export interface DrawingStrokeEndEvent extends DrawingEvent {
  type: "STROKE_END"
}

export interface DrawingCanvasClearEvent extends DrawingEvent {
  type: "CANVAS_CLEAR"
}
