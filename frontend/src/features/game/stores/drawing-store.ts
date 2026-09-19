import { useStore } from "zustand/react"
import { createStore } from "zustand/vanilla"
import { DEFAULT_DRAWING_COLOUR, DEFAULT_DRAWING_TOOL } from "../config/drawing"
import {
  DrawingColour,
  DrawingPoint,
  DrawingStroke,
  DrawingTool,
} from "../types/round/phase/drawing/drawing"

export interface LiveDrawingState {
  points: DrawingPoint[]
  colour: string
  tool: DrawingTool
}

interface DrawingStoreState {
  strokes: DrawingStroke[]
  localDrawing: LiveDrawingState | null
  remoteDrawers: Map<string, LiveDrawingState>
  tool: DrawingTool
  colour: DrawingColour
  revision: number
}

interface DrawingStoreActions {
  startLocalStroke: (
    point: DrawingPoint,
    colour: string,
    tool: DrawingTool
  ) => void
  addLocalPoint: (point: DrawingPoint) => void
  commitLocalStroke: () => DrawingStroke | null
  startRemoteStroke: (
    userId: string,
    tool: DrawingTool,
    point: DrawingPoint,
    colour: string
  ) => void
  addRemotePoints: (userId: string, points: DrawingPoint[]) => void
  commitRemoteStroke: (userId: string) => void
  clearRemoteDrawer: (userId: string) => void
  clearCanvas: () => void
  loadStrokes: (strokes: DrawingStroke[]) => void
  setTool: (tool: DrawingTool) => void
  setColour: (colour: DrawingColour) => void
}

type DrawingStore = DrawingStoreState & DrawingStoreActions

// uses zustland vanilla store, not the React-hook version.
// the renderer subscribes to it imperatively so 30fps point
// updates never trigger a React re-render.
export const drawingStore = createStore<DrawingStore>((set, get) => ({
  strokes: [],
  localDrawing: null,
  remoteDrawers: new Map(),
  tool: DEFAULT_DRAWING_TOOL,
  colour: DEFAULT_DRAWING_COLOUR,
  revision: 0,

  startLocalStroke: (point, colour, tool) =>
    set({ localDrawing: { points: [point], colour, tool } }),

  addLocalPoint: (point) =>
    set((s) =>
      s.localDrawing
        ? {
            localDrawing: {
              ...s.localDrawing,
              points: [...s.localDrawing.points, point],
            },
          }
        : {}
    ),

  commitLocalStroke: () => {
    const current = get().localDrawing
    set({ localDrawing: null })
    if (!current || current.points.length === 0) return null
    const stroke: DrawingStroke = {
      points: current.points,
      colour: current.colour,
      tool: current.tool,
    }
    set((s) => ({ strokes: [...s.strokes, stroke], revision: s.revision + 1 }))
    return stroke
  },

  startRemoteStroke: (userId, tool, point, colour) =>
    set((s) => ({
      remoteDrawers: new Map(s.remoteDrawers).set(userId, {
        points: [point],
        colour,
        tool,
      }),
    })),

  addRemotePoints: (userId, points) =>
    set((s) => {
      const existing = s.remoteDrawers.get(userId)
      if (!existing) return {}
      return {
        remoteDrawers: new Map(s.remoteDrawers).set(userId, {
          ...existing,
          points: [...existing.points, ...points],
        }),
      }
    }),

  commitRemoteStroke: (userId) =>
    set((s) => {
      const existing = s.remoteDrawers.get(userId)
      if (!existing || existing.points.length === 0) return {}
      const stroke: DrawingStroke = {
        points: existing.points,
        colour: existing.colour,
        tool: existing.tool,
      }
      const remoteDrawers = new Map(s.remoteDrawers)
      remoteDrawers.delete(userId)
      return {
        remoteDrawers,
        strokes: [...s.strokes, stroke],
        revision: s.revision + 1,
      }
    }),

  clearRemoteDrawer: (userId) =>
    set((s) => {
      const remoteDrawers = new Map(s.remoteDrawers)
      remoteDrawers.delete(userId)
      return { remoteDrawers }
    }),

  clearCanvas: () =>
    set((s) => ({
      strokes: [],
      localDrawing: null,
      remoteDrawers: new Map(),
      revision: s.revision + 1,
    })),

  loadStrokes: (strokes) =>
    set((s) => ({
      strokes,
      localDrawing: null,
      remoteDrawers: new Map(),
      revision: s.revision + 1,
    })),

  setTool: (tool) => set({ tool }),
  setColour: (colour) => set({ colour }),
}))

// for components that need reactive reads.
// the canvas renderer uses drawingStore.getState().subscribe() directly
export const useDrawingStore = <T>(selector: (state: DrawingStore) => T) =>
  useStore(drawingStore, selector)

export const useDrawingTool = () => useDrawingStore((s) => s.tool)
export const useDrawingColour = () => useDrawingStore((s) => s.colour)
export const useDrawingSetTool = () => useDrawingStore((s) => s.setTool)
export const useDrawingSetColour = () => useDrawingStore((s) => s.setColour)
