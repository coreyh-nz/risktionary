// features/game/hooks/canvas/use-canvas-snapshot.ts
import { RefObject, useEffect, useRef } from "react"
import {
  SNAPSHOT_IMAGE_TYPE,
  SNAPSHOT_INTERVAL_MS,
  SNAPSHOT_MAX_DIMENSION,
} from "../../config/drawing"
import { drawingStore } from "../../stores/drawing-store"

export const useCanvasSnapshot = (
  canvasRef: RefObject<HTMLCanvasElement | null>,
  onSnapshot: (blob: Blob) => void
) => {
  const onSnapshotRef = useRef(onSnapshot)
  const lastSentRevisionRef = useRef(-1)
  const scratchCanvasRef = useRef<HTMLCanvasElement | null>(null)

  useEffect(() => {
    onSnapshotRef.current = onSnapshot
  }, [onSnapshot])

  useEffect(() => {
    const interval = setInterval(() => {
      const source = canvasRef.current
      if (!source) return

      const revision = drawingStore.getState().revision
      if (revision === lastSentRevisionRef.current) return
      lastSentRevisionRef.current = revision

      const scale = Math.min(
        1,
        SNAPSHOT_MAX_DIMENSION / Math.max(source.width, source.height)
      )
      if (scale === 1) {
        source.toBlob((blob) => {
          if (blob) onSnapshotRef.current(blob)
        }, SNAPSHOT_IMAGE_TYPE)
        return
      }

      if (!scratchCanvasRef.current)
        scratchCanvasRef.current = document.createElement("canvas")
      const scratch = scratchCanvasRef.current
      scratch.width = Math.round(source.width * scale)
      scratch.height = Math.round(source.height * scale)

      const ctx = scratch.getContext("2d")
      if (!ctx) return
      ctx.drawImage(source, 0, 0, scratch.width, scratch.height)

      scratch.toBlob((blob) => {
        if (blob) onSnapshotRef.current(blob)
      }, SNAPSHOT_IMAGE_TYPE)
    }, SNAPSHOT_INTERVAL_MS)
    return () => clearInterval(interval)
  }, [canvasRef])
}
