"use client"

import { Slider } from "@/components/ui/slider"
import { cva } from "class-variance-authority"
import { cn } from "cn"
import * as React from "react"

export interface Level<Id extends string = string> {
  id: Id
  value: number
  label: string
}

const labelVariants = cva(
  "absolute top-0 -translate-x-1/2 whitespace-nowrap text-xs font-medium transition-colors",
  {
    variants: {
      active: {
        true: "text-foreground",
        false: "text-muted-foreground",
      },
    },
    defaultVariants: {
      active: false,
    },
  }
)

interface LevelSliderProps<Id extends string> {
  levels: readonly Level<Id>[]
  value: Id
  onValueChange: (id: Id) => void
  className?: string
}

export const LevelSlider = <TId extends string>({
  levels,
  value,
  onValueChange,
  className,
}: LevelSliderProps<TId>) => {
  const sorted = React.useMemo(
    () => [...levels].sort((a, b) => a.value - b.value),
    [levels]
  )
  const min = sorted[0]?.value ?? 0
  const max = sorted[sorted.length - 1]?.value ?? 0
  const span = max - min || 1

  const valueToId = React.useMemo(
    () => new Map(sorted.map((l) => [l.value, l.id])),
    [sorted]
  )
  const idToValue = React.useMemo(
    () => new Map(sorted.map((l) => [l.id, l.value])),
    [sorted]
  )

  const currentValue = idToValue.get(value) ?? min

  const handleChange = (next: number | readonly number[]) => {
    const numeric = typeof next === "number" ? next : next[0]
    const id = valueToId.get(numeric)
    if (id !== undefined) onValueChange(id)
  }

  return (
    // px-* reserves room for the outermost labels, which overflow
    // past the 0%/100% track points by half their own width.
    <div className={cn("w-full px-8", className)}>
      <Slider
        value={[currentValue]}
        onValueChange={handleChange}
        min={min}
        max={max}
        step={1}
      />

      <div className="relative mt-2 h-4 w-full">
        {sorted.map((level) => (
          <span
            key={level.id}
            style={{ left: `${((level.value - min) / span) * 100}%` }}
            className={labelVariants({ active: level.id === value })}
          >
            {level.label}
          </span>
        ))}
      </div>
    </div>
  )
}
