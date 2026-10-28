import {
  RiskMatrixCell,
  RiskMatrixCellProps,
} from "@/features/game/components/round/phase/rating/matrix/risk-matrix-cell"
import { cva, VariantProps } from "class-variance-authority"
import { cn } from "cn"

export type DensityTier = "none" | "low" | "medium" | "high" | "max"

const riskMatrixDensityCellFillVariants = cva(
  "absolute inset-0 bg-primary transition-opacity",
  {
    variants: {
      tier: {
        none: "opacity-0",
        low: "opacity-30",
        medium: "opacity-50",
        high: "opacity-70",
        max: "opacity-90",
      },
    },
    defaultVariants: { tier: "none" },
  }
)

interface RiskMatrixDensityCellProps
  extends
    Required<VariantProps<typeof riskMatrixDensityCellFillVariants>>,
    RiskMatrixCellProps {
  count: number
}

export const RiskMatrixDensityCell = ({
  count,
  tier,
  ...props
}: RiskMatrixDensityCellProps) => {
  return (
    <RiskMatrixCell {...props}>
      <span className={cn(riskMatrixDensityCellFillVariants({ tier }))} />
      <span
        className={cn(
          "absolute inset-0 flex items-center justify-center text-sm font-black tabular-nums",
          count === 0
            ? "text-muted-foreground/40"
            : tier === "high" || tier === "max"
              ? "text-primary-foreground"
              : "text-foreground"
        )}
      >
        {count > 0 ? count : ""}
      </span>
    </RiskMatrixCell>
  )
}
