import { DrawingColour } from "@/features/game/types/round/phase/drawing/drawing"

const colourDefinitions = {
  BLACK: {
    name: "Black",
    value: "oklch(14.1% 0.005 285.823)",
    tailwind: "bg-zinc-950",
  },
  WHITE: {
    name: "White",
    value: "oklch(100% 0 0)",
    tailwind: "bg-white",
  },
  RED: {
    name: "Red",
    value: "oklch(63.7% 0.237 25.331)",
    tailwind: "bg-red-500",
  },
  ORANGE: {
    name: "Orange",
    value: "oklch(70.5% 0.213 47.604)",
    tailwind: "bg-orange-500",
  },
  YELLOW: {
    name: "Yellow",
    value: "oklch(79.5% 0.184 86.047)",
    tailwind: "bg-yellow-500",
  },
  GREEN: {
    name: "Green",
    value: "oklch(72.3% 0.219 149.579)",
    tailwind: "bg-green-500",
  },
  TEAL: {
    name: "Teal",
    value: "oklch(70.4% 0.14 182.503)",
    tailwind: "bg-teal-500",
  },
  SKY: {
    name: "Sky",
    value: "oklch(68.5% 0.169 237.323)",
    tailwind: "bg-sky-500",
  },
  INDIGO: {
    name: "Indigo",
    value: "oklch(58.5% 0.233 277.117)",
    tailwind: "bg-indigo-500",
  },
  PINK: {
    name: "Pink",
    value: "oklch(65.6% 0.241 354.308)",
    tailwind: "bg-pink-500",
  },
  BROWN: {
    name: "Brown",
    value: "oklch(47.3% 0.137 46.201)",
    tailwind: "bg-amber-800",
  },
  GRAY: {
    name: "Gray",
    value: "oklch(70.5% 0.015 286.067)",
    tailwind: "bg-zinc-400",
  },
} as const

export const PALETTE_COLOURS = colourDefinitions

export const PALETTE_COLOURS_LIST: DrawingColour[] =
  Object.values(colourDefinitions)
