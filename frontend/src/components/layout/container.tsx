import { cn } from "cn"

import { cva, type VariantProps } from "class-variance-authority"
import { HTMLAttributes } from "react"

export const containerVariants = cva("w-full mx-auto p-4", {
  variants: {
    size: {
      sm: "max-w-sm",
      md: "max-w-md",
      lg: "max-w-lg",
      xl: "max-w-2xl",
      "2xl": "max-w-3xl",
      "3xl": "max-w-4xl",
      full: "max-w-full",
    },
  },
  defaultVariants: {
    size: "lg",
  },
})

interface ContainerProps
  extends
    HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof containerVariants> {}

export const Container = ({
  size = "lg",
  className,
  children,
  ...rest
}: ContainerProps) => {
  return (
    <div className={cn(containerVariants({ size }), className)} {...rest}>
      {children}
    </div>
  )
}
