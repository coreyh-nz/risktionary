import { cn } from "cn"

export const StepIndicator = ({
  className,
  children,
  ...props
}: React.ComponentProps<"div">) => {
  return (
    <div
      className={cn(
        "flex shrink-0 h-[1.75em] w-[1.75em] items-center justify-center rounded-full bg-primary text-sm font-semibold text-primary-foreground",
        className
      )}
      {...props}
    >
      {children}
    </div>
  )
}
