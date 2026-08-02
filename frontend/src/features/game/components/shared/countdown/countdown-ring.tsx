"use client"

import { cn } from "@/lib/utils"

const dimensions = {
  sm: { radius: 20, strokeWidth: 3, boxSize: 46 },
  md: { radius: 28, strokeWidth: 4, boxSize: 64 },
  lg: { radius: 52, strokeWidth: 5, boxSize: 114 },
  xl: { radius: 88, strokeWidth: 6, boxSize: 188 },
} as const

type RingSize = keyof typeof dimensions

interface CountdownRingProps {
  secondsLeft: number
  total: number
  size?: RingSize
  progressClassName?: string
  trackClassName?: string
  className?: string
}

export const CountdownRing = ({
  secondsLeft,
  total,
  size = "md",
  progressClassName,
  trackClassName,
  className,
}: CountdownRingProps) => {
  const { radius, strokeWidth, boxSize } = dimensions[size]
  const circumference = 2 * Math.PI * radius
  const offset = circumference * (1 - secondsLeft / Math.max(1, total))
  const center = boxSize / 2

  return (
    <svg
      width={boxSize}
      height={boxSize}
      viewBox={`0 0 ${boxSize} ${boxSize}`}
      className={cn("-rotate-90", className)}
      aria-hidden
    >
      {/* Track */}
      <circle
        cx={center}
        cy={center}
        r={radius}
        fill="none"
        stroke="currentColor"
        strokeWidth={strokeWidth}
        className={cn("text-muted", trackClassName)}
      />
      {/* Progress */}
      <circle
        cx={center}
        cy={center}
        r={radius}
        fill="none"
        stroke="currentColor"
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeDasharray={circumference}
        strokeDashoffset={offset}
        className={cn(
          "text-primary transition-all duration-200 ease-linear",
          progressClassName
        )}
      />
    </svg>
  )
}
