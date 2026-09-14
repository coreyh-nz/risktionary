import { cn } from "cn"

import { HTMLAttributes } from "react"

export const PageSection = ({
  className,
  ...props
}: HTMLAttributes<HTMLElement>) => {
  return <section className={cn("flex flex-col gap-8", className)} {...props} />
}
