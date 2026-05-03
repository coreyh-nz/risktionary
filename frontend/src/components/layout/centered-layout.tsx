// components/layout/centered-layout.tsx
import { cva, type VariantProps } from "class-variance-authority"
import { PropsWithChildren } from "react"
import { cn } from "@/lib/utils"

const centeredVariants = cva("flex min-h-svh items-center justify-center p-6", {
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

interface CenteredLayoutProps
  extends PropsWithChildren, VariantProps<typeof centeredVariants> {}

export const CenteredLayout = ({
  children,
  size = "lg",
}: CenteredLayoutProps) => {
  return (
    <div className="flex min-h-svh items-center justify-center">
      <div className={cn(centeredVariants({ size }), "w-full p-6")}>
        {children}
      </div>
    </div>
  )
}
