import { PropsWithChildren, useState } from "react"
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
  // Use <a> instead of <Link> because this href points to an external backend
  // OAuth endpoint. <Link> would prefetch the URL as an RSC request and intercept
  // the redirect response, breaking the OAuth flow.
  return (
    <a
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
    </a>
  )
}
