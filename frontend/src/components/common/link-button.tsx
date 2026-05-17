"use client"
import { buttonVariants } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { VariantProps } from "class-variance-authority"
import Link from "next/link"
import { ComponentProps } from "react"

type LinkButtonProps = ComponentProps<typeof Link> &
  VariantProps<typeof buttonVariants> & {
    disabled?: boolean
  }

export const LinkButton = ({
  variant,
  size,
  className,
  disabled,
  children,
  ...props
}: LinkButtonProps) => {
  if (disabled) {
    return (
      <a
        className={cn(
          buttonVariants({ variant, size }),
          className,
          "pointer-events-none opacity-50"
        )}
        aria-disabled="true"
        role="link"
      >
        {children}
      </a>
    )
  }

  return (
    <Link
      className={cn(buttonVariants({ variant, size }), className)}
      {...props}
    >
      {children}
    </Link>
  )
}
