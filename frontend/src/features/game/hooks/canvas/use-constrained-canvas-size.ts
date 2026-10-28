import { DRAWING_ASPECT_RATIO } from "@/features/game/config/drawing"
import { useLayoutEffect, useRef, useState } from "react"

/**
 * Computes the largest possible canvas size that:
 *   1. Fits within the available width and height of a wrapper element
 *   2. Maintains the drawing aspect ratio (DRAWING_ASPECT_RATIO)
 *   3. Accounts for an optional toolbar sitting below the canvas
 *
 * Usage: attach `wrapperRef` to the outer container div, and `toolbarRef` to
 * the toolbar div if one is present. `size` will be null until the first
 * measurement fires, after which it updates reactively on resize.
 *
 * Sizing logic:
 *   - Start by fitting to the full available width
 *   - If the resulting height exceeds the available height (minus toolbar),
 *     clamp to height and derive width from the aspect ratio instead
 */
export const useConstrainedCanvasSize = () => {
  const wrapperRef = useRef<HTMLDivElement>(null)
  const toolbarRef = useRef<HTMLDivElement>(null)
  const [size, setSize] = useState<{ width: number; height: number } | null>(
    null
  )

  useLayoutEffect(() => {
    const wrapper = wrapperRef.current
    if (!wrapper) return

    const calculate = () => {
      const availableWidth = wrapper.clientWidth
      const availableHeight = wrapper.clientHeight
      const toolbarHeight = toolbarRef.current
        ? toolbarRef.current.offsetHeight + 12 // gap-3
        : 0

      const maxWidth = availableWidth
      const maxHeight = availableHeight - toolbarHeight

      let w = maxWidth
      let h = w / DRAWING_ASPECT_RATIO

      if (h > maxHeight) {
        h = maxHeight
        w = h * DRAWING_ASPECT_RATIO
      }

      setSize({ width: Math.floor(w), height: Math.floor(h) })
    }

    const observer = new ResizeObserver(calculate)
    observer.observe(wrapper)
    if (toolbarRef.current) observer.observe(toolbarRef.current)

    return () => observer.disconnect()
  }, [])

  return { wrapperRef, toolbarRef, size }
}
