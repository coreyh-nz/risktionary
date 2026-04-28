import { PropsWithChildren, useState } from "react"
import Link from "next/link"
import { cn } from "@/lib/utils"
import { buttonVariants } from "@/components/ui/button"
import { Spinner } from "@/components/ui/spinner"

interface OAuthButtonProps extends PropsWithChildren {
  href: string
  className?: string
}

export const OAuthButton = ({
  href,
  children,
  className,
}: OAuthButtonProps) => {
  const [loading, setLoading] = useState(false)

  return (
    <Link
      href={href}
      onClick={() => setLoading(true)}
      className={cn(
        buttonVariants({ variant: "outline" }),
        "flex flex-1 items-center justify-center gap-2",
        loading && "pointer-events-none opacity-70",
        className
      )}
    >
      {loading ? <Spinner /> : children}
    </Link>
  )
}
