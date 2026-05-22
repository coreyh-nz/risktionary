import { ComponentPropsWithoutRef, Ref } from "react"

interface CanvasProps extends ComponentPropsWithoutRef<"canvas"> {
  canvasRef: Ref<HTMLCanvasElement>
}

export const Canvas = ({ canvasRef, ...rest }: CanvasProps) => {
  return (
    <div className="relative overflow-hidden rounded-xl border-2 border-border bg-card shadow-sm">
      <canvas ref={canvasRef} className="touch-none block" {...rest} />
    </div>
  )
}
