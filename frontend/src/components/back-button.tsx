"use client"

import { Button, buttonVariants } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { ReactNode } from "react"

type BackLinkProps =
  | {
      href: string
      children?: ReactNode
      className?: string
      onClick?: never
    }
  | {
      href?: never
      children?: ReactNode
      className?: string
      onClick?: () => void
    }

export const BackLink = ({
  href,
  children = "Back",
  className,
  onClick,
}: BackLinkProps) => {
  const router = useRouter()

  if (href) {
    return (
      <Link href={href} className={cn("w-fit", buttonVariants(), className)}>
        <ArrowLeft />
        {children}
      </Link>
    )
  }

  return (
    <Button
      type="button"
      variant="ghost"
      className={cn("w-fit", className)}
      onClick={onClick ?? (() => router.back())}
    >
      <ArrowLeft />
      {children}
    </Button>
  )
}
