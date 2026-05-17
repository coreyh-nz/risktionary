import { ComponentPropsWithoutRef, Ref } from "react"

interface CanvasProps extends ComponentPropsWithoutRef<"canvas"> {
  containerRef: Ref<HTMLDivElement>
  canvasRef: Ref<HTMLCanvasElement>
  canvasReady: boolean
}

export const Canvas = ({
  containerRef,
  canvasRef,
  canvasReady,
  ...rest
}: CanvasProps) => {
  return (
    <div
      ref={containerRef}
      className="relative flex-1 overflow-hidden rounded-xl border-2 border-border bg-card shadow-sm"
    >
      <canvas
        ref={canvasRef}
        className="absolute inset-0 touch-none"
        {...rest}
      />
      {!canvasReady && (
        <div className="absolute inset-0 flex items-center justify-center bg-card">
          <p className="text-muted-foreground">Loading canvas...</p>
        </div>
      )}
    </div>
  )
}
