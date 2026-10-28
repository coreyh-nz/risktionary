import { cn } from "cn"

import { cva, type VariantProps } from "class-variance-authority"
import { Card } from "./card"

const cardStyleVariants = cva("border", {
  variants: {
    border: {
      solid: "",
      dashed: "border-dashed",
    },
    variant: {
      default: "bg-muted border-muted-foreground",
      selected: "bg-primary/10 border-primary text-primary",
    },
  },
  defaultVariants: {
    border: "solid",
    variant: "default",
  },
})

type CardStyleProps = VariantProps<typeof cardStyleVariants>

function OutlinedCard({
  className,
  border = "dashed",
  variant,
  children,
  ...props
}: React.ComponentProps<typeof Card> & CardStyleProps) {
  return (
    <Card
      className={cn(cardStyleVariants({ border, variant }), className)}
      {...props}
    >
      {children}
    </Card>
  )
}

function SelectableCard({
  className,
  selected,
  border,
  onSelect,
  children,
  ...props
}: React.ComponentProps<typeof Card> &
  Omit<CardStyleProps, "variant"> & {
    selected?: boolean
    onSelect?: () => void
  }) {
  return (
    <Card
      className={cn(
        cardStyleVariants({
          border,
          variant: selected ? "selected" : "default",
        }),
        className
      )}
      onClick={onSelect}
      role="button"
      {...props}
    >
      {children}
    </Card>
  )
}

export { cardStyleVariants, OutlinedCard, SelectableCard }
