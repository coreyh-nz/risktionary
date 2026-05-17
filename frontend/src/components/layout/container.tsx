import { cn } from "@/lib/utils"
import { cva, type VariantProps } from "class-variance-authority"
import { PropsWithChildren } from "react"

export const containerVariants = cva("w-full mx-auto p-6", {
  variants: {
    size: {
      sm: "max-w-sm",
      md: "max-w-md",
      lg: "max-w-lg",
      xl: "max-w-2xl",
      "2xl": "max-w-3xl",
      "3xl": "max-w-4xl",
      full: "max-w-full px-4",
    },
  },
  defaultVariants: {
    size: "lg",
  },
})

interface ContainerProps
  extends PropsWithChildren, VariantProps<typeof containerVariants> {}

export const Container = ({ children, size = "lg" }: ContainerProps) => {
  return <div className={cn(containerVariants({ size }))}>{children}</div>
}
