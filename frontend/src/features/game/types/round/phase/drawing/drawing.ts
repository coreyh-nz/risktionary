export type DrawingTool = "PEN" | "ERASER"

export interface DrawingPoint {
  x: number
  y: number
}

export interface DrawingStroke {
  points: DrawingPoint[]
  colour: string
  tool: DrawingTool
}

export interface DrawingColour {
  name: string
  value: string
}
