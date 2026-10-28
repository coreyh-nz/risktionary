"use client"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"
import { useEffect, useRef } from "react"

const digitVariants = cva(
  "leading-none font-black text-foreground tabular-nums",
  {
    variants: {
      size: {
        sm: "text-sm",
        md: "text-3xl",
        lg: "text-5xl",
        xl: "text-8xl",
      },
    },
    defaultVariants: { size: "md" },
  }
)

interface CountdownDigitProps extends VariantProps<typeof digitVariants> {
  secondsLeft: number
  animated?: boolean
  className?: string
}

export const CountdownDigit = ({
  secondsLeft,
  size,
  animated = true,
  className,
}: CountdownDigitProps) => {
  const ref = useRef<HTMLSpanElement>(null)
  const prevRef = useRef(secondsLeft)

  useEffect(() => {
    if (!animated) return

    const element = ref.current
    if (!element || prevRef.current === secondsLeft) return

    const animation = element.animate(
      [
        { transform: "scale(1.4)", opacity: 0.6 },
        { transform: "scale(1)", opacity: 1 },
      ],
      {
        duration: 300,
        easing: "cubic-bezier(0.4, 0, 0.2, 1)",
      }
    )
    prevRef.current = secondsLeft

    return () => animation.cancel()
  }, [secondsLeft, animated])

  return (
    <span
      ref={ref}
      className={cn(digitVariants({ size }), className)}
      aria-live="assertive"
      aria-label={`${secondsLeft} seconds remaining`}
    >
      {secondsLeft}
    </span>
  )
}
