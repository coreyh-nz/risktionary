import { cn } from "cn"

import * as React from "react"
import { PropsWithChildren } from "react"

type TextProps = React.HTMLAttributes<HTMLElement>

export const PageHeader = ({
  children,
  className,
  ...rest
}: PropsWithChildren & TextProps) => {
  return (
    <div className={cn("space-y-2", className)} {...rest}>
      {children}
    </div>
  )
}

export const PageTitle = ({ className, ...props }: TextProps) => {
  return (
    <h1
      className={cn(
        "text-3xl font-bold tracking-tight text-foreground",
        className
      )}
      {...props}
    />
  )
}

export const PageDescription = ({
  className,
  ...props
}: React.HTMLAttributes<HTMLParagraphElement>) => {
  return <p className={cn("text-muted-foreground", className)} {...props} />
}

export const SectionTitle = ({ className, ...props }: TextProps) => {
  return (
    <h2
      className={cn(
        "text-2xl font-semibold tracking-tight text-foreground",
        className
      )}
      {...props}
    />
  )
}

export const SectionDescription = ({
  className,
  ...props
}: React.HTMLAttributes<HTMLParagraphElement>) => {
  return (
    <p className={cn("text-sm text-muted-foreground", className)} {...props} />
  )
}

export const LeadText = ({
  className,
  ...props
}: React.HTMLAttributes<HTMLParagraphElement>) => {
  return (
    <p className={cn("text-lg text-muted-foreground", className)} {...props} />
  )
}

export const MutedText = ({
  className,
  ...props
}: React.HTMLAttributes<HTMLParagraphElement>) => {
  return (
    <p className={cn("text-sm text-muted-foreground", className)} {...props} />
  )
}
