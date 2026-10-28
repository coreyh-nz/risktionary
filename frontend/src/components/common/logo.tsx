import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"
import { TriangleAlert } from "lucide-react"

import { ROUTES } from "@/lib/routes"
import Link from "next/link"

const logoVariants = cva("flex items-center font-black tracking-tight", {
  variants: {
    size: {
      sm: "gap-1.5 text-lg [&>svg]:size-5",
      md: "gap-2 text-xl [&>svg]:size-6",
      lg: "gap-2.5 text-4xl [&>svg]:size-10",
      xl: "gap-3 text-5xl [&>svg]:size-12",
    },
  },
  defaultVariants: {
    size: "md",
  },
})

type LogoProps = VariantProps<typeof logoVariants> & {
  className?: string
  textClassName?: string
  showText?: boolean
}

export const Logo = ({
  size,
  className,
  textClassName,
  showText = true,
}: LogoProps) => {
  return (
    <Link href={ROUTES.HOME} className={cn(logoVariants({ size }), className)}>
      <TriangleAlert className="stroke-3 text-primary" />

      {showText && (
        <span className={cn(textClassName)}>
          <span className="text-primary">Risk</span>
          tionary
        </span>
      )}
    </Link>
  )
}
