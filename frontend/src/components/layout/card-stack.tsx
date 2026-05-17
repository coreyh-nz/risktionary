import { cn } from "@/lib/utils"
import { HTMLAttributes } from "react"

export const CardStack = ({
  className,
  ...props
}: HTMLAttributes<HTMLDivElement>) => {
  return <div className={cn("flex flex-col gap-4", className)} {...props} />
}
