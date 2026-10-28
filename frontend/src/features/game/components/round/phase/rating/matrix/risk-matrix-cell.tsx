import { cn } from "cn"
import { ComponentProps } from "react"

export type RiskMatrixCellProps = ComponentProps<"div">

export const RiskMatrixCell = ({
  className,
  ...props
}: RiskMatrixCellProps) => {
  return (
    <div
      className={cn(
        "relative aspect-square overflow-hidden rounded-lg bg-muted ring-1 ring-inset ring-border transition-all",
        className
      )}
      {...props}
    />
  )
}
