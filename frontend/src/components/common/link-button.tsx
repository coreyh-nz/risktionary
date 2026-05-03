"use client"

import { buttonVariants } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import Link from "next/link"
import { ComponentProps } from "react"
import { VariantProps } from "class-variance-authority"

interface LinkButtonProps
  extends ComponentProps<typeof Link>, VariantProps<typeof buttonVariants> {
  className?: string
}

export const LinkButton = ({
  href,
  className,
  variant,
  size,
  children,
  ...props
}: LinkButtonProps) => {
  return (
    <Link
      href={href}
      className={cn(buttonVariants({ variant, size }), className)}
      {...props}
    >
      {children}
    </Link>
  )
}
